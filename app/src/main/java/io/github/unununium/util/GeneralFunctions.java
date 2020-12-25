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

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.InputDevice;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/** Static functions that are used throughout the app. **/
public class GeneralFunctions {
    private GeneralFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Exits the app.**/
    public static void exitApp(@NotNull Activity activity) {
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /** Gets the external screenshots directory of the app.
     * It first tries to find the screenshot folder, and falls back to the DCIM directory
     * and subsequently the photos directory if it fails.
     * Then, it falls back to the download directory.
     * The download directory is assumed to be /storage/emulated/0/Download or /storage/emulated/0/Downloads.
     * If it doesn't exist, it falls back to getInternalScreenshotsDir.
     * The path will always end in '/'. **/
    @NonNull
    public static String getExternalScreenshotsDir(@NonNull Context context) {
        File[] externalDirList = new File[]{ new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Screenshots"),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DCIM).getAbsolutePath() + "/screenshots"),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DCIM).getAbsolutePath() + "/Screenshot"),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DCIM).getAbsolutePath() + "/screenshot"),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DCIM).getAbsolutePath()),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_PICTURES).getAbsolutePath()),
                new File(Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DOWNLOADS).getAbsolutePath())};
        for (File file: externalDirList) {
            if (file.exists() && file.isDirectory() && file.canWrite()) return file.getAbsolutePath() + "/";
        }
        return getInternalScreenshotsDir(context);
    }

    /** Get the internal screenshots directory of the app.
     * Falls back to the root directory if no such directory internally could be found.
     * The path will always end in '/'. **/
    @NonNull
    private static String getInternalScreenshotsDir(@NonNull Context context) {
        File downloadDirFile = null;
        File[] internalDirList = new File[]{context.getExternalFilesDir(Environment.DIRECTORY_DCIM),
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) };
        for (File file: internalDirList) {
            if (file != null) return file.getAbsolutePath();
        }
        return downloadDirFile == null ? "/storage/emulated/0/" : downloadDirFile.getAbsolutePath() + "/";
    }

    /** Generates a valid file in the required directory.
     * If a file with the same name exists,
     * a file with incrementing number will be added to the file.
     * @param extension needs to include the . at the front.**/
    public static String generateValidFile(String filename, String extension) {
        String returnFile = filename + extension;
        int i = 1;
        while (new File(returnFile).exists() && i < Integer.MAX_VALUE) {
            returnFile = filename + "(" + i + ")" + extension;
            i++;
        }
        return returnFile;
    }

    /** Checks if the device has the required controller features (game pad, joystick, BUTTON_L1,
     * BUTTON_R1, AXIS_LTRIGGER, AXIS_RTRIGGER) **/
    public static boolean deviceIsController(@NotNull InputDevice device) {
        int sources = device.getSources();
        return (sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD &&
                (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
    }
}
