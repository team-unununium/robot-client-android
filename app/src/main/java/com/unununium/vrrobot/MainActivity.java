package com.unununium.vrrobot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** M1: The main activity for the project. This is based on the sample at
 * https://github.com/googlevr/gvr-android-sdk **/
public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glView;

    // ****** Inherited functions ****** //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up views
        setImmersiveSticky();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                (visibility) -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        setImmersiveSticky();
                    }
                });

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

        // Set up graphics
        glView = findViewById(R.id.surface_view);
        glView.setEGLContextClientVersion(2);
        Renderer renderer = new Renderer();
        glView.setRenderer(renderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    // ****** Display related functions ****** //
    private class Renderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            // TODO: Show live video feed
        }
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

    // ****** Other functions ****** //

    private void exitApp() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
