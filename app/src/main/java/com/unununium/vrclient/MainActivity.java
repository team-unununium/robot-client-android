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

package com.unununium.vrclient;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.unununium.vrclient.about.AboutActivity;
import com.unununium.vrclient.program.ControllerActivity;
import com.unununium.vrclient.program.ObserverActivity;

import java.util.UUID;

/*
* TODO:
*  1) Main Activity (Done)
*  2) AboutActivity (Done)
*  3) ControllerActivity
*  4) ObserverActivity
* */
public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce;

    // TODO: Move to each activity
    // Drawable lists
    public static int[] TEMP_LIST = new int[]{R.drawable.ic_temp_cold,
            R.drawable.ic_temp_default, R.drawable.ic_temp_hot};
    public static int[] GAS_LIST = new int[]{R.drawable.ic_gas_co, R.drawable.ic_gas_ch4,
            R.drawable.ic_gas_h2, R.drawable.ic_gas_lpg};
    public static int[] HUMIDITY_LIST = new int[]{R.drawable.ic_humidity_low,
            R.drawable.ic_humidity_medium, R.drawable.ic_humidity_high};

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
        }

        // Check for permissions
        if (!permissionsEnabled()) {
            requestPermissions();
        }

        new Thread(() -> {
            if (!GeneralFunctions.checkServerOnline(this)) {
                Toast.makeText(MainActivity.this, "Server offline", Toast.LENGTH_SHORT).show();
            }
        }).start();

        // Set up listeners
        findViewById(R.id.m1_controller).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.m1_observer).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ObserverActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.m1_about).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        // Press back to exit
        if (doubleBackToExitPressedOnce) {
            exitApp();
        } else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
        }
    }

    private void exitApp() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
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

    private void launchPermissionsSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
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
}
