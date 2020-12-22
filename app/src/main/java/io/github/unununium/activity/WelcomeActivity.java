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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import io.github.unununium.R;
import io.github.unununium.util.GeneralFunctions;

/** The activity that welcomes first-time users and checks for permissions.
 * It should not be the first activity, but it is used to check for permissions that
 * the app requires to function. **/
public class WelcomeActivity extends AppIntro {
    private int totalSlideCount = 0;
    private SharedPreferences sharedPref;
    private static final String PREF_INTRO_DONE = "intro_done";
    private static final String[] BT_PERM_LIST = { Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN };
    private static final String[] LOC_PERM_LIST =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    new String[]{ Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION } :
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
    private static final String[] STORAGE_PERM_LIST = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE };

    /** Adds the slides for the app and checks whether the permissions for the app are met. **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
        checkGuid();
        setIntroAttributes();
        if (!sharedPref.getBoolean(PREF_INTRO_DONE, false)) addWelcomeSlide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addBluetoothPermissions();
            addLocationPermissions();
            addStoragePermissions();
        }
    }

    /** Sets up the GUID for the device. **/
    private void checkGuid() {
        if (sharedPref.getString("guid", "").length() == 0) {
            String guid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("guid", guid);
            editor.apply();
        }
    }

    /** Set the intro attributes for the app. **/
    private void setIntroAttributes() {
        // Immersive mode is removed as it doesn't work well with custom fragments
        // Although no custom fragments are currently used, it is still kept just in case
        setIndicatorEnabled(true);
        setProgressIndicator();
        setTransformer(AppIntroPageTransformerType.Fade.INSTANCE);
    }

    /** Adds the welcome slide to the app only if its not shown before. **/
    private void addWelcomeSlide() {
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_1_title),
                getString(R.string.intro_slide_1_desc),
                R.drawable.ic_appintro_next, // Image Drawable
                ContextCompat.getColor(WelcomeActivity.this, R.color.colorPrimary), // Background color
                Color.WHITE, Color.WHITE)); // Title and description color
        totalSlideCount += 1;
    }

    /** Adds the slides that request the Bluetooth permissions for the app. **/
    private void addBluetoothPermissions() {
        boolean btPermissionGranted = ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        if (!btPermissionGranted) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_2_title),
                    getString(R.string.intro_slide_2_desc),
                    R.drawable.ic_bluetooth_50, // Image Drawable
                    ContextCompat.getColor(WelcomeActivity.this, R.color.colorPrimary), // Background color
                    Color.WHITE, Color.WHITE)); // Title and description color
            totalSlideCount += 1;
            askForPermissions(BT_PERM_LIST, totalSlideCount, false);
        }
    }

    /** Adds the slides that request the location permissions for the app. **/
    private void addLocationPermissions() {
        boolean locPermissionGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locPermissionGranted = (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(WelcomeActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        } else {
            locPermissionGranted = ContextCompat.checkSelfPermission(WelcomeActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        if (!locPermissionGranted) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_3_title),
                    getString(R.string.intro_slide_3_desc),
                    R.drawable.ic_location_50, // Image Drawable
                    ContextCompat.getColor(WelcomeActivity.this, R.color.colorPrimary), // Background color
                    Color.WHITE, Color.WHITE)); // Title and description color
            totalSlideCount += 1;
            askForPermissions(LOC_PERM_LIST, totalSlideCount, false);
        }
    }

    /** Adds the slides that request the storage permissions for the app. **/
    private void addStoragePermissions() {
        boolean storagePermissionGranted = (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!storagePermissionGranted) {
            addSlide(AppIntroFragment.newInstance(getString(R.string.intro_slide_4_title),
                    getString(R.string.intro_slide_4_desc),
                    R.drawable.ic_folder_50, // Image Drawable
                    ContextCompat.getColor(WelcomeActivity.this, R.color.colorPrimary), // Background color
                    Color.WHITE, Color.WHITE)); // Title and description color
            totalSlideCount += 1;
            askForPermissions(STORAGE_PERM_LIST, totalSlideCount, false);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        // Prevents IndexOutOfBounds for having no slides
        if (totalSlideCount > 0) {
            super.onPostCreate(savedInstanceState);
        } else {
            super.onPause();
            onIntroFinished();
        }
    }

    /** The user has disabled the permissions that are required to access the app. **/
    @Override
    protected void onUserDisabledPermission(@NotNull String permissionName) {
        super.onUserDisabledPermission(permissionName);
        if (permissionName.contains("BLUETOOTH")) {
            Toast.makeText(WelcomeActivity.this, R.string.error_bluetooth_permission_denied, Toast.LENGTH_LONG).show();
        } else if (permissionName.contains("LOCATION")) {
            Toast.makeText(WelcomeActivity.this, R.string.error_location_permission_denied, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(WelcomeActivity.this, R.string.error_storage_permission_denied, Toast.LENGTH_LONG).show();
        }
    }

    /** Goes to the home page when the intro is finished. **/
    @Override
    protected void onIntroFinished() {
        super.onIntroFinished();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_INTRO_DONE, true);
        editor.apply();
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /** Exits the app. **/
    @Override
    public void onBackPressed() {
        GeneralFunctions.exitApp(WelcomeActivity.this);
    }
}
