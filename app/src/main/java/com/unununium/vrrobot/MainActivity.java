package com.unununium.vrrobot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.vr.ndk.base.GvrLayout;
import com.unununium.vrrobot.utils.VideoExoPlayer2;
import com.unununium.vrrobot.utils.VideoSceneRenderer;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** M1: The main activity for the project. This is based on the samples at
 * https://github.com/googlevr/gvr-android-sdk **/
public class MainActivity extends AppCompatActivity {
    // Drawable lists
    public static int[] TEMP_LIST = new int[]{R.drawable.ic_temp_cold,
            R.drawable.ic_temp_default, R.drawable.ic_temp_hot};
    public static int[] GAS_LIST = new int[]{R.drawable.ic_gas_co, R.drawable.ic_gas_ch4,
        R.drawable.ic_gas_h2, R.drawable.ic_gas_lpg};
    public static int[] HUMIDITY_LIST = new int[]{R.drawable.ic_humidity_low,
            R.drawable.ic_humidity_medium, R.drawable.ic_humidity_high};

    // String lists
    public static String SERVER_CLIENT_SECRET = "xKMvUxW9BDUNkpzkfesHaW5eq8yQpxJN1EBV5qmq0sEFacPyCz";
    public static String SERVER_URL = "https://unununium-vr-server.herokuapp.com";

    // VR Video Variables
    private GvrLayout gvrLayout;
    private GLSurfaceView surfaceView;
    private VideoSceneRenderer renderer;
    private VideoExoPlayer2 videoPlayer;
    // Runnable to refresh the viewer profile when gvrLayout is resumed.
    // This is done on the GL thread because refreshViewerProfile isn't thread-safe.
    private final Runnable refreshViewerProfileRunnable = new Runnable() {
        @Override
        public void run() {
            gvrLayout.getGvrApi().refreshViewerProfile();
        }
    };

    // Motion sensor variables
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float MIN_ROTATION = 0.001f;
    private static final double EPSILON = 1;
    private final float[] deltaRotationVector = new float[3];
    private float timestamp;

    // Socket & sensor variables
    private boolean startedMoving = false;
    private Socket socket;
    private OkHttpClient client = new OkHttpClient();
    private SensorEventListener sensorListener;
    private SensorManager sensorManager;
    private Sensor sensor;

    // ****** Inherited functions ****** //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up GUID
        String guid;
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        if (sharedPref.getString("guid", "").length() == 0) {
            guid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("guid", guid);
            editor.apply();
        } else {
            guid = sharedPref.getString("guid", "");
        }

        // Set up views (Including Surface View)
        setImmersiveSticky();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                (visibility) -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        setImmersiveSticky();
                    }
                });
        gvrLayout = new GvrLayout(this);
        surfaceView = findViewById(R.id.m1_footage);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(5, 6, 5, 0, 0, 0);
        gvrLayout.setPresentationView(surfaceView);
        gvrLayout.setKeepScreenOn(true);
        renderer = new VideoSceneRenderer(this, gvrLayout.getGvrApi());
        // Initialize the ExternalSurfaceListener to receive video Surface callbacks.
        surfaceView.setRenderer(renderer);

        // Check for permissions
        if (!permissionsEnabled()) {
            requestPermissions();
        }

        // Set up button listeners
        findViewById(R.id.m1_info).setOnClickListener(v -> {
            Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.m1_back).setOnClickListener(v -> exitApp());
        findViewById(R.id.m1_touch_btn).setOnClickListener(v -> {
            if (startedMoving && socket != null) {
                socket.emit("clientStartMoving", "");
            } else if (socket != null) {
                socket.emit("clientStopMoving", "");
            }
            startedMoving = !startedMoving;
        });

        new Thread(() -> {
            // Check for network activity
            if(!checkServerOnline()) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        R.string.m1_not_connected, Toast.LENGTH_SHORT).show());
                return;
            }

            // Check token
            String token;
            if (sharedPref.getString("token", "").length() == 0) {
                token = requestToken(guid, sharedPref);
                if (token == null) {
                    runOnUiThread(() -> runOnUiThread(() -> Toast.makeText(this,
                            R.string.m1_network_error, Toast.LENGTH_SHORT).show()));
                    return;
                }
            } else {
                token = sharedPref.getString("token", "");
            }

            // Get icons
            ImageView tempImg = findViewById(R.id.m1_temp_icon);
            ImageView gasImg = findViewById(R.id.m1_gas_icon);
            ImageView humidityImg = findViewById(R.id.m1_humidity_icon);
            ImageView frontObstacleImg = findViewById(R.id.m1_front_obstacle);
            ImageView obstacleImg = findViewById(R.id.m1_obstacle);
            ImageView backObstacleImg = findViewById(R.id.m1_back_obstacle);

            // Set up sensors
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) : null;

            // Connect to socket.io with token
            try {
                IO.Options options = new IO.Options();
                options.query = "client";
                socket = IO.socket(SERVER_URL, options);
                socket.on(Socket.EVENT_CONNECT, args -> {
                    HashMap<String, String> jsonMap = new HashMap<>();
                    jsonMap.put("guid", guid);
                    jsonMap.put("token", token);
                    socket.emit("authentication", new JSONObject(jsonMap).toString());
                }).on("authorized",
                        args -> socket.emit("clientRequestData"))
                        .on("unauthorized", args ->
                                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Hunch",
                                        Toast.LENGTH_SHORT).show()))
                        .on("clientDataReceived", args -> {
                            try {
                                JSONObject object = new JSONObject(args[0].toString());
                                // Get updated values
                                int temp = object.getInt("temp"),
                                        gasType = object.getInt("gas"),
                                        humidity = object.getInt("humidity");
                                boolean frontObstacle = object.getBoolean("frontObstacle"),
                                        backObstacle = object.getBoolean("backObstacle");

                                // Update values to show on screen
                                runOnUiThread(() -> {
                                    tempImg.setImageDrawable(getDrawable(TEMP_LIST[temp]));
                                    if (gasType == 0) {
                                        gasImg.setImageDrawable(getDrawable(android.R.color.transparent));
                                    } else {
                                        gasImg.setImageDrawable(getDrawable(GAS_LIST[gasType - 1]));
                                    }
                                    humidityImg.setImageDrawable(getDrawable(HUMIDITY_LIST[humidity + 1]));
                                    if (!frontObstacle && !backObstacle) {
                                        frontObstacleImg .setImageDrawable(getDrawable(android.R.color.transparent));
                                        obstacleImg.setImageDrawable(getDrawable(android.R.color.transparent));
                                        backObstacleImg.setImageDrawable(getDrawable(android.R.color.transparent));
                                    } else {
                                        obstacleImg.setImageDrawable(getDrawable(R.drawable.ic_obstacle));
                                        if (frontObstacle) {
                                            frontObstacleImg.setImageDrawable(getDrawable(R.drawable.ic_obstacle_front));
                                        }
                                        if (backObstacle) {
                                            backObstacleImg.setImageDrawable(getDrawable(R.drawable.ic_obstacle_back));
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                Toast.makeText(MainActivity.this,
                                        R.string.m1_processing_error, Toast.LENGTH_SHORT).show();
                            }
                        })
                .on("clientStreamUrl", args -> {
                    String url = (String) args[0];
                    initVideoPlayer(url);
                });
                socket.connect();

                // Set up thread to update direction
                sensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        // This timestep's delta rotation to be multiplied by the current rotation
                        // after computing it from the gyro sample data.
                        if (timestamp != 0) {
                            final float dT = (event.timestamp - timestamp) * NS2S;
                            // Axis of the rotation sample, not normalized yet.
                            float axisX = event.values[0];
                            float axisY = event.values[1];
                            float axisZ = event.values[2];

                            // Calculate the angular speed of the sample
                            float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                            // Normalize the rotation vector if it's big enough to get the axis
                            // (that is, EPSILON should represent your maximum allowable margin of error)
                            if (omegaMagnitude > EPSILON) {
                                axisX /= omegaMagnitude;
                                axisY /= omegaMagnitude;
                                axisZ /= omegaMagnitude;
                            }

                            // Integrate around this axis with the angular speed by the timestep
                            // in order to get a delta rotation from this sample over the timestep
                            // We will convert this axis-angle representation of the delta rotation
                            // into a quaternion before turning it into the rotation matrix.
                            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                            deltaRotationVector[0] = sinThetaOverTwo * axisX;
                            deltaRotationVector[1] = sinThetaOverTwo * axisY;
                            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                        }
                        timestamp = event.timestamp;


                        // Check if values are large enough to warrant a rotation
                        boolean thresholdReached = false;
                        for (float rotation: deltaRotationVector) {
                            if (rotation > MIN_ROTATION && socket != null) {
                                thresholdReached = true;
                                break;
                            }
                        }
                        if (thresholdReached) {
                            HashMap<String, Float> jsonMap = new HashMap<>();
                            jsonMap.put("X", deltaRotationVector[0]);
                            jsonMap.put("Y", deltaRotationVector[1]);
                            jsonMap.put("Z", deltaRotationVector[2]);
                            socket.emit("clientRotateCamera", new JSONObject(jsonMap).toString());
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
            } catch (URISyntaxException e) {
                Log.w(getString(R.string.app_name), getString(R.string.m1_internal_error));
                e.printStackTrace();
            }
        }).start();
    }

    private void initVideoPlayer() {
        initVideoPlayer("");
    }
    private void initVideoPlayer(String url) {
        videoPlayer = new VideoExoPlayer2(getApplication());
        Uri streamUri;
        streamUri = Uri.parse(url);

        videoPlayer.initPlayer(streamUri);
        renderer.setVideoPlayer(videoPlayer);
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (videoPlayer == null) {
            initVideoPlayer();
        }
        surfaceView.queueEvent(() -> renderer.setHasVideoPlaybackStarted(false));

        // Resume the surfaceView and gvrLayout here. This will start the render thread and trigger a
        // new async reprojection video Surface to become available.
        surfaceView.onResume();
        gvrLayout.onResume();
        // Refresh the viewer profile in case the viewer params were changed.
        surfaceView.queueEvent(refreshViewerProfileRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorListener != null && sensorManager != null && sensor != null) {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorListener != null && sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

    @Override
    public void onStop() {
        if (videoPlayer != null) {
            renderer.setVideoPlayer(null);
            renderer.shutdown();
            videoPlayer.releasePlayer();
            videoPlayer = null;
        }
        // Pause the gvrLayout and surfaceView here. The video Surface is guaranteed to be detached and
        // not available after gvrLayout.onPause(). We pause from onStop() to avoid needing to wait
        // for an available video Surface following brief onPause()/onResume() events. Wait for the
        // new onSurfaceAvailable() callback with a valid Surface before resuming the video player.
        gvrLayout.onPause();
        surfaceView.onPause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        gvrLayout.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(@NotNull KeyEvent event) {
        // Avoid accidental volume key presses while the phone is in the VR headset.
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    // ****** Sticky Related functions ****** //

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveSticky();
        }
    }

    private void setImmersiveSticky() {
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // ****** Permission related functions ******//

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean permissionsEnabled() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        final String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
        int READ_STORAGE_PERMISSION = 134;
        ActivityCompat.requestPermissions(this, permissions, READ_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!permissionsEnabled()) {
            Toast.makeText(this, R.string.m1_request_permissions, Toast.LENGTH_LONG).show();
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Permission denied with checking "Do not ask again".
                launchPermissionsSettings();
            }
            finish();
        }
    }

    private void launchPermissionsSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    // ****** Network functions ****** //

    private boolean checkServerOnline() {
        // Check if network is connected
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    isConnected = ni.isConnected();
                }
            } else {
                final Network n = cm.getActiveNetwork();
                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                    if (nc != null) {
                        isConnected = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    }
                }
            }
        }
        if (!isConnected) {
            return false;
        }

        Request request = new Request.Builder().url(SERVER_URL + "/test").build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response.code() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    @org.jetbrains.annotations.Nullable
    private String requestToken(String guid, SharedPreferences sharedPref) {
        try {
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("guid", guid);
            bodyObj.put("secret", SERVER_CLIENT_SECRET);
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),bodyObj.toString());
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/access")
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() == 201 && response.body() != null) {
                try {
                    String token = new JSONObject(response.body().string()).getString("token");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("token", token);
                    editor.apply();
                    return token;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ****** Other functions ****** //

    private void exitApp() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
