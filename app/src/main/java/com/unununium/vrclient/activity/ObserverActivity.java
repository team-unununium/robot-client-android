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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.MainActivity;
import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.NetworkFunctions;
import com.unununium.vrclient.functions.UIFunctions;

import io.socket.client.Socket;
import okhttp3.OkHttpClient;

public class ObserverActivity extends AppCompatActivity {
    private WebView webView;
    private Socket socket;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer);

        findViewById(R.id.m2_back).setOnClickListener(v -> returnToMain());

        // Icons shown here for readability
        ImageView tempImg = findViewById(R.id.m1_temp_icon),
                gasImg = findViewById(R.id.m1_gas_icon),
                humidityImg = findViewById(R.id.m1_humidity_icon),
                frontObstacleImg = findViewById(R.id.m1_front_obstacle),
                obstacleImg = findViewById(R.id.m1_obstacle),
                backObstacleImg = findViewById(R.id.m1_back_obstacle);
        new Thread(() -> socket = NetworkFunctions.initSocket(ObserverActivity.this, tempImg,
                gasImg, humidityImg, frontObstacleImg, obstacleImg, backObstacleImg)).start();

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
        returnToMain();
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

    private void returnToMain() {
        // Delete data
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            boolean deletionSuccessful = NetworkFunctions
                    .deleteToken(getSharedPreferences(getPackageName(), MODE_PRIVATE), client);
            if (deletionSuccessful) {
                socket.close();
            } else {
                Toast.makeText(ObserverActivity.this, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }
        }).start();
        Intent intent = new Intent(ObserverActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
