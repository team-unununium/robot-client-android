/*
 * This program is the client app for Team Unununium's VR Robot Explorer found at <https://github.com/team-unununium>
 * Copyright (C) 2020 Team Unununium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/> .
 */

package com.unununium.vrclient.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.NetworkFunctions;
import com.unununium.vrclient.functions.UIFunctions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.Socket;

public class ControllerActivity extends AppCompatActivity {
    private WebView webView;
    private Socket socket;

    // Motion sensor variables
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float MIN_ROTATION = 0.001f;
    private static final double EPSILON = 1;
    private final float[] deltaRotationVector = new float[3];
    private float timestamp;
    private boolean startedMoving = false;

    // Sensor objects
    private SensorEventListener sensorListener;
    private SensorManager sensorManager;
    private Sensor sensor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        // Set up listeners
        findViewById(R.id.m1_back).setOnClickListener(v -> NetworkFunctions
                .returnToMain(ControllerActivity.this, socket));
        findViewById(R.id.m1_touch_btn).setOnClickListener(v -> {
            if (startedMoving && socket != null) {
                socket.emit("clientStartMoving", "");
            } else if (socket != null) {
                socket.emit("clientStopMoving", "");
            }
            startedMoving = !startedMoving;
        });

        // Icons shown here for readability
        ImageView tempImg = findViewById(R.id.m1_temp_icon),
                gasImg = findViewById(R.id.m1_gas_icon),
                humidityImg = findViewById(R.id.m1_humidity_icon),
                frontObstacleImg = findViewById(R.id.m1_front_obstacle),
                obstacleImg = findViewById(R.id.m1_obstacle),
                backObstacleImg = findViewById(R.id.m1_back_obstacle);
        new Thread(() -> socket = NetworkFunctions.initSocket(ControllerActivity.this, tempImg,
                gasImg, humidityImg, frontObstacleImg, obstacleImg, backObstacleImg)).start();

        webView = findViewById(R.id.m1_twitch);
        webView.setOnTouchListener((v, event) -> true);

        // Set up sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) : null;
        sensorListener = initSensorListener();
        if (sensorManager == null) {
            Toast.makeText(ControllerActivity.this, R.string.sensor_manager_missing, Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            UIFunctions.setImmersiveSticky(this);
        }
    }

    @Override
    public void onBackPressed() {
        NetworkFunctions.returnToMain(ControllerActivity.this, socket);
    }

    @Override
    public void onPause() {
        super.onPause();
        UIFunctions.clearWebView(webView);
        // Pause sensor listener
        if (sensorListener != null && sensorManager != null) {
            sensorManager.unregisterListener(sensorListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // WebView is needed for the 3D View as well
        UIFunctions.initWebView(webView);
        // Start sensor listener
        if (sensorListener != null && sensorManager != null && sensor != null) {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private SensorEventListener initSensorListener() {
        return new SensorEventListener() {
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
    }
}
