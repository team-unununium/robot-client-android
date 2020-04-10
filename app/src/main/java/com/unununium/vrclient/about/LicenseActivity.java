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

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pcchin.licenseview.LicenseView;
import com.unununium.vrclient.R;

public class LicenseActivity extends AppCompatActivity {
    private static final int[] licenseArrays = new int[]{ R.array.study_assistant_license,
            R.array.licenseview_license, R.array.okhttp_license, R.array.socketio_client_java_license };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        LicenseView licenseView = findViewById(R.id.license_view);
        for (int licenseArray : licenseArrays) {
            // Updates license info & OnClickListeners
            String[] infoArray = getResources().getStringArray(licenseArray);
            licenseView.addLicense(infoArray);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LicenseActivity.this, AboutActivity.class);
        startActivity(intent);
    }
}
