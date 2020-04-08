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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unununium.vrclient.BuildConfig;
import com.unununium.vrclient.functions.FileFunctions;
import com.unununium.vrclient.functions.UIFunctions;
import com.unununium.vrclient.MainActivity;
import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.AutoDismissDialog;

import java.util.Calendar;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set version
        TextView textView = findViewById(R.id.m2_version);
        textView.setText(String.format("%s%s", getString(R.string.m2_version), BuildConfig.VERSION_NAME));

        // Set current year
        TextView copyrightView = findViewById(R.id.m2_copyright);
        copyrightView.setText(String.format(Locale.ENGLISH, "%s%d %s",
                getString(R.string.m2_copyright_p1), Calendar.getInstance().get(Calendar.YEAR),
                getString(R.string.m2_copyright_p2)));

        findViewById(R.id.m2_library_license).setOnClickListener(view -> {
            Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.m2_rss_license).setOnClickListener(view ->
                new AutoDismissDialog(getString(R.string.m2_rss_used), getString(R.string.l1_license),
                        new String[]{"", getString(R.string.close), ""},
                        new DialogInterface.OnClickListener[]{null, null, null})
                        .show(getSupportFragmentManager(), "AboutActivity.Rss"));

        // Set license text
        UIFunctions.setHtml(findViewById(R.id.m2_apache), FileFunctions.getTxt(
                this, "vr_client_about.txt"));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AboutActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
