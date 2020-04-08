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
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.BuildConfig;
import com.unununium.vrclient.MainActivity;
import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.UIFunctions;
import com.unununium.vrclient.functions.NetworkFunctions;

import okhttp3.OkHttpClient;

public class ObserverActivity extends AppCompatActivity {
    private WebView webView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer);

        findViewById(R.id.m2_back).setOnClickListener(v -> {
            Intent intent = new Intent(ObserverActivity.this, MainActivity.class);
            startActivity(intent);
        });

        new Thread(() -> {
            // Set up socket connection
            OkHttpClient client = new OkHttpClient();
            String token = NetworkFunctions.requestToken(BuildConfig.SERVER_CLIENT_SECRET,
                    getSharedPreferences(getPackageName(), MODE_PRIVATE), client);
            if (token == null) {
                runOnUiThread(() -> Toast.makeText(ObserverActivity.this, R.string.a_network_error,
                        Toast.LENGTH_SHORT).show());
            } else {
                // TODO: Socket
            }
        }).start();

        webView = findViewById(R.id.m2_twitch);
        webView.setOnTouchListener((v, event) -> true);
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
        Intent intent = new Intent(ObserverActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        UIFunctions.clearWebView(webView);
    }

    @Override
    public void onResume() {
        super.onResume();
        UIFunctions.initWebView(webView);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Delete data
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            boolean deletionSuccessful = NetworkFunctions
                    .deleteToken(getSharedPreferences(getPackageName(), MODE_PRIVATE), client);
            if (deletionSuccessful) {
                // TODO: Stop socket
            } else {
                Toast.makeText(ObserverActivity.this, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }
        }).start();
    }
}
