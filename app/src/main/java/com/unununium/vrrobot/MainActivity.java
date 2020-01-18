package com.unununium.vrrobot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private native long nativeOnCreate(AssetManager assetManager);

    private native void nativeOnDestroy(long nativeApp);

    private native void nativeOnSurfaceCreated(long nativeApp);

    private native void nativeOnDrawFrame(long nativeApp);

    private native void nativeOnTriggerEvent(long nativeApp);

    private native void nativeOnPause(long nativeApp);

    private native void nativeOnResume(long nativeApp);

    private native void nativeSetScreenParams(long nativeApp, int width, int height);

    private native void nativeSwitchViewer(long nativeApp);
}
