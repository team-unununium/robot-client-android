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
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.NetworkFunctions;
import com.unununium.vrclient.functions.UIFunctions;

import io.socket.client.Socket;

public class ObserverActivity extends AppCompatActivity {
    private WebView webView;
    private Socket socket;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observer);

        findViewById(R.id.m2_back).setOnClickListener(v -> NetworkFunctions
                .returnToMain(ObserverActivity.this, socket));

        // Icons shown here for readability
        ImageView tempImg = findViewById(R.id.m2_temp_icon),
                gasImg = findViewById(R.id.m2_gas_icon),
                humidityImg = findViewById(R.id.m2_humidity_icon),
                frontObstacleImg = findViewById(R.id.m2_front_obstacle),
                obstacleImg = findViewById(R.id.m2_obstacle),
                backObstacleImg = findViewById(R.id.m2_back_obstacle);
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
        NetworkFunctions.returnToMain(ObserverActivity.this, socket);
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
}
