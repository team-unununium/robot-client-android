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
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.unununium.BuildConfig;
import io.github.unununium.R;
import io.github.unununium.util.GeneralFunctions;

/** The main Activity for the app, handles displaying the videos only
 * as the UI is handled by the overlay fragments. **/
public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.m1_webview);
        webView.setOnTouchListener((v, event) -> true); // Intercept all touch events
    }

    /** Set the top bar of the screen to be hidden. **/
    private void setImmersiveSticky() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /** Exits the app if the back button is pressed twice. **/
    @Override
    public void onBackPressed() {
        // Press back to exit
        if (doubleBackToExitPressedOnce) {
            GeneralFunctions.exitApp(MainActivity.this);
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.activity_back_exit, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
        }
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
        webSettings.setAppCacheEnabled(false);
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
}