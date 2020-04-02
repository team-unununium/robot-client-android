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

package com.unununium.vrclient.about;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.GeneralFunctions;
import com.unununium.vrclient.R;

import java.util.Arrays;
import java.util.Objects;

public class LicenseActivity extends AppCompatActivity {
    private static final int[] licenseArrays = new int[]{ R.array.study_assistant_license,
            R.array.okhttp_license, R.array.socketio_client_java_license };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressLint("InflateParams")
        ScrollView returnScroll = (ScrollView) getLayoutInflater().inflate(R.layout.blank_list, null, false);
        LinearLayout returnLayout = returnScroll.findViewById(R.id.blank_linear);

        for (int licenseArray : licenseArrays) {
            // Updates license info & OnClickListeners
            String[] infoArray = getResources().getStringArray(licenseArray);
            if (infoArray.length == 3 && getFragmentManager() != null) {
                @SuppressLint("InflateParams") LinearLayout licenseDisplay = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.license_display, null);
                // Common functions used by all licenses for neatness
                ((TextView) licenseDisplay.findViewById(R.id.m4_lib)).setText(infoArray[1]);

                if (Objects.equals(infoArray[0], getString(R.string.license_apache_2))) {
                    // Apache 2.0 license
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_apache_2);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(this);
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + GeneralFunctions.getTxt(this,
                                        "apache_2_license.txt"));
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.license_apache_2)
                                .setView(licenseView)
                                .setNegativeButton(R.string.close, (dialog, i) -> dialog.dismiss())
                                .create().show();
                    });
                } else if (Objects.equals(infoArray[0], getString(R.string.license_mit))) {
                    // MIT license
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_mit);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(this);
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + GeneralFunctions.getTxt(this,
                                        "mit_license.txt"));
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.license_mit)
                                .setView(licenseView)
                                .setNegativeButton(R.string.close, (dialog, i) -> dialog.dismiss())
                                .create().show();
                    });
                } else if (Objects.equals(infoArray[0], getString(R.string.license_cc_3_unported))) {
                    // Creative Commons CC 3.0 Unported
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_cc_3_unported);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(this);
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + GeneralFunctions.getTxt(this,
                                        "cc_3_unported.txt"));
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.license_cc_3_unported)
                                .setView(licenseView)
                                .setNegativeButton(R.string.close, (dialog, i) -> dialog.dismiss())
                                .create().show();
                    });
                } else if (Objects.equals(infoArray[0], getString(R.string.license_gnu_gpl_v3))) {
                    // GNU GPL V3 License
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_gnu_gpl_v3);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(this);
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + GeneralFunctions.getTxt(this,
                                        "gnu_gpl_v3_license.txt"));
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.license_gnu_gpl_v3)
                                .setView(licenseView)
                                .setNegativeButton(R.string.close, (dialog, i) -> dialog.dismiss())
                                .create().show();
                    });
                }
                returnLayout.addView(licenseDisplay);
            } else {
                Log.w(getString(R.string.app_name), "XML Error: Incorrect CharSequence[] in " +
                        "license_display read, value is " + Arrays.toString(infoArray));
            }
        }
        setContentView(returnScroll);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LicenseActivity.this, AboutActivity.class);
        startActivity(intent);
    }
}
