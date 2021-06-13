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

package io.github.unununium.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;
import io.github.unununium.fragment.OverlayFragment;

/** An extension to MainActivity that handles the input received by buttons and controllers. **/
public class InputHandler {
    public boolean isDay = true;
    private final MainActivity parent;

    public InputHandler(MainActivity parent) {
        this.parent = parent;
    }

    /** When a screenshot command is received. Takes a "screenshot" of the FrameLayout and saves it
     * as a bitmap image. **/
    public void onScreenshot() {
        // TODO: Update code for VideoView
        VideoView layout = parent.findViewById(R.id.m1_playerview);
        layout.setDrawingCacheEnabled(true);
        // https://stackoverflow.com/a/4618030/8141824
        layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        layout.buildDrawingCache(true);
        Bitmap bitmap = layout.getDrawingCache();
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.ENGLISH);
        String filePath = String.format("%s%s",
                GeneralFunctions.getExternalScreenshotsDir(parent), format.format(currentDate));
        String generatedFilePath = GeneralFunctions
                .generateValidFile(filePath, ".png");
        // Writes the bitmap to the file, checks whether the path is valid in the process
        try (FileOutputStream out = new FileOutputStream(generatedFilePath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(parent, String.format("%s%s", "File saved to ", generatedFilePath),
                    Toast.LENGTH_LONG).show();
        } catch (NullPointerException | IOException e) {
            Toast.makeText(parent, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /** When the command to invert the colour of the UI is called. **/
    public void onInvertColour() {
        isDay = !isDay;
        if (parent.currentFragment != null && parent.currentFragment instanceof OverlayFragment) {
            ((OverlayFragment) parent.currentFragment).swapColour(isDay);
        }
    }

    /** When the command to hide the overlay is called. **/
    public void onToggleUI() {
        boolean uiShouldBeShown = parent.uiIsHidden;
        if (uiShouldBeShown) {
            showCurrentFragment();
        } else {
            parent.showOverlay(Constants.OverlayType.TYPE_NONE);
        }
    }

    /** When the command to toggle the upper overlay is called. **/
    public void onToggleUpperOverlay() {
        parent.upperOverlayIsHidden = !parent.upperOverlayIsHidden;
        if (!parent.uiIsHidden) {
            showCurrentFragment();
        }
    }

    /** When the command to toggle the diagnostics mode is called. **/
    public void onToggleDiagnosticsMode() {
        parent.diagnosticsModeEnabled = !parent.diagnosticsModeEnabled;
        if (!parent.uiIsHidden && parent.upperOverlayIsHidden) {
            showCurrentFragment();
        }
    }

    /** Shows the current fragment that is set by the activity, ignoring whether the UI is showing. **/
    private void showCurrentFragment() {
        if (parent.upperOverlayIsHidden) {
            if (parent.diagnosticsModeEnabled) parent.showOverlay(Constants.OverlayType.TYPE_DIAGNOSTICS);
            else if (parent.normalOverlayIsText) parent.showOverlay(Constants.OverlayType.TYPE_NORMAL_TEXT);
            else parent.showOverlay(Constants.OverlayType.TYPE_NORMAL_ICON);
        } else {
            if (parent.upperOverlayIsSettings) parent.showOverlay(Constants.OverlayType.TYPE_SETTINGS);
            else parent.showOverlay(Constants.OverlayType.TYPE_ABOUT);
        }
    }
}
