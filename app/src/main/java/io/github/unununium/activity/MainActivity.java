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

package io.github.unununium.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

import io.github.unununium.BuildConfig;
import io.github.unununium.R;
import io.github.unununium.fragment.AboutOverlayFragment;
import io.github.unununium.fragment.DiagnosticsOverlayFragment;
import io.github.unununium.fragment.NormalOverlayFragment;
import io.github.unununium.fragment.SettingsOverlayFragment;
import io.github.unununium.util.Constants;
import io.github.unununium.util.FragmentOnBackPressed;
import io.github.unununium.util.GeneralFunctions;
import io.github.unununium.util.InputHandler;

/** The main Activity for the app, handles displaying the videos only
 * as the UI is handled by the overlay fragments. **/
public class MainActivity extends AppCompatActivity {
    private Constants.OverlayType currentOverlayType = Constants.OverlayType.TYPE_NONE;
    private InputHandler inputHandler = null;
    private Fragment currentFragment = null;
    private WebView webView;
    private int lastControllerID = 0;
    private boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputHandler = new InputHandler(MainActivity.this);
        webView = findViewById(R.id.m1_webview);
        webView.setOnTouchListener((v, event) -> true); // Intercept all touch events
        webView.setOnKeyListener((v, keyCode, event) -> false); // Leave the keycode events to the activity
        View overlayView = findViewById(R.id.m1_overlay);
        overlayView.setOnKeyListener((v, keyCode, event) -> false);
        // Show the text view by default
        showOverlay(Constants.OverlayType.TYPE_NORMAL_TEXT);
    }

    /** Pauses the live stream. **/
    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) clearWebView();
    }

    /** Clears a specific WebView. **/
    private void clearWebView() {
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setDomStorageEnabled(false);
        webSettings.setDatabaseEnabled(false);
        webSettings.setMixedContentMode(0);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.loadUrl("about:blank");
    }

    /** Resumes the live stream. **/
    @Override
    protected void onResume() {
        super.onResume();
        setImmersiveSticky();
        if (webView != null) initWebView();
    }

    /** Set the top bar of the screen to be hidden. **/
    private void setImmersiveSticky() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /** Initializes the WebView to play the embed video from the server. **/
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        String currentUrl = BuildConfig.SERVER_URL + "/embed";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.loadUrl(currentUrl);
    }

    /** Exits the app if the back button is pressed twice. **/
    @Override
    public void onBackPressed() {
        // Handle FragmentOnBackPressed
        if (currentFragment == null || (currentFragment instanceof FragmentOnBackPressed
                && ((FragmentOnBackPressed) currentFragment).onBackPressed())) {
            // Press back to exit
            if (doubleBackToExitPressedOnce) {
                GeneralFunctions.exitApp(MainActivity.this);
            } else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, R.string.activity_back_exit, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (!GeneralFunctions.deviceIsController(event.getDevice())) {
            return false;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_X:
                // TODO: Start / stop moving
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                // TODO: Invert UI colour
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                // TODO: Toggle UI visibility
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                // TODO: Toggle camera night mode
                break;
            case KeyEvent.KEYCODE_BUTTON_L1:
                inputHandler.onScreenshot();
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                // TODO: Diagnostics mode
                break;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_MENU:
                // TODO: Show settings
                break;
            default:
                return false;
        }
        lastControllerID = event.getDeviceId();
        return true;
    }

    /** Show a specific overlay. **/
    public void showOverlay(@NotNull Constants.OverlayType overlayType) {
        Fragment targetFragment;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch (overlayType) {
            case TYPE_ABOUT:
                targetFragment = new AboutOverlayFragment(MainActivity.this);
                break;
            case TYPE_DIAGNOSTICS:
                targetFragment = new DiagnosticsOverlayFragment(MainActivity.this);
                break;
            case TYPE_NORMAL_TEXT:
                targetFragment = new NormalOverlayFragment(MainActivity.this, true);
                break;
            case TYPE_NORMAL_ICON:
                targetFragment = new NormalOverlayFragment(MainActivity.this, false);
                break;
            case TYPE_SETTINGS:
                targetFragment = new SettingsOverlayFragment(MainActivity.this);
                break;
            default:
                // Used to hide the UI
                targetFragment = null;
                break;
        }
        if (currentFragment == null) {
            currentOverlayType = Constants.OverlayType.TYPE_NONE;
        } else {
            if (targetFragment == null) {
                ft.remove(currentFragment);
                currentFragment = null;
            } else {
                if (currentFragment instanceof SettingsOverlayFragment) {
                    // TODO: Cancel settings
                }
                ft.replace(R.id.m1_overlay, targetFragment, "MainActivity.Overlay");
                currentFragment = targetFragment;
                currentOverlayType = overlayType;
            }
            ft.commit();
        }
    }
}