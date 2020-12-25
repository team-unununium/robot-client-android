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
import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;

/** An extension to MainActivity that handles the input received by buttons and controllers. **/
public class InputHandler {
    private final MainActivity parent;

    public InputHandler(MainActivity parent) {
        this.parent = parent;
    }

    /** When a screenshot command is received. Takes a "screenshot" of the WebView and saves it
     * as a bitmap image. **/
    public void onScreenshot() {
        WebView webView = parent.findViewById(R.id.m1_webview);
        if (webView != null) {
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss", Locale.ENGLISH);
            String filePath = String.format("%s%s",
                    GeneralFunctions.getExternalScreenshotsDir(parent), format.format(currentDate));
            String generatedFilePath = GeneralFunctions
                    .generateValidFile(filePath, ".png");
            Bitmap result = loadBitmapFromView(webView);
            // Writes the bitmap to the file, checks whether the path is valid in the process
            try (FileOutputStream out = new FileOutputStream(generatedFilePath)) {
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(parent, String.format("%s%s", "File saved to ", generatedFilePath),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Takes a snapshot of the current view as a bitmap. **/
    public static Bitmap loadBitmapFromView(@NotNull View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth() , v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}
