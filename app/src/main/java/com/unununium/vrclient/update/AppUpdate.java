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

package com.unununium.vrclient.update;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.unununium.vrclient.BuildConfig;
import com.unununium.vrclient.MainActivity;
import com.unununium.vrclient.R;
import com.unununium.vrclient.functions.AutoDismissDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Class that is used to check github for updates to the app, separated from
 * @see MainActivity for clarity.
 * Cannot be made static as githubReleasesStatusCode needs to be passed on from function to function. **/
public class AppUpdate {
    /* Example user agent: "VR-Client/1.5 (...)" */
    @SuppressWarnings("ConstantConditions")
    private static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("VR-Client/%s", BuildConfig.VERSION_NAME));
    private static final String UPDATE_PATH = "/client/latest";

    private final boolean calledFromNotif;
    private final MainActivity activity;

    /** Checks for version updates of the app, doubles as the constructor.
     * checkServerUpdates() separated for clarity. **/
    public AppUpdate(@NonNull MainActivity activity, boolean calledFromNotif) {
        this.activity = activity;
        this.calledFromNotif = calledFromNotif;

        // Check if network is connected (Updated code as old code is deprecated)
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    isConnected = ni.isConnected();
                }
            } else {
                final Network n = cm.getActiveNetwork();
                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                    if (nc != null) {
                        isConnected = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    }
                }
            }
        }

        // Check if there is a newer version of the app
        if (isConnected) {
            checkServerUpdates();
        }
    }

    /** Checks whether a newer version of the app has been released on GitHub through the main api,
     * and checks the backup API if the main API fails,
     * separated from constructor for clarity,
     * showUpdateNotif(JSONArray response) separated for clarity. */
    private void checkServerUpdates() {
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Main Server
        JsonObjectRequest getReleases = new JsonObjectRequest(BuildConfig.SERVER_URL + UPDATE_PATH, null,
                this::showUpdateNotif, error -> {
            Log.d("com.unununium.vrclient", "Network Error: Volley returned error " +
                    error.getMessage() + ":" + error.toString() + " from " + BuildConfig.SERVER_URL
                    + ", stack trace is");
            error.printStackTrace();
            queue.stop();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", USER_AGENT);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        // Send request
        queue.add(getReleases);
    }

    /** Show users the update notification,
     * separated from checkServerUpdates() for clarity,
     * updateViaGithub(String downloadLink) separated for clarity. **/
    private void showUpdateNotif(@NonNull JSONObject response) {
        try {
            // Get latest version from releases page
            if (!Objects.equals(response.getString("version")
                    .replace("v", ""), BuildConfig.VERSION_NAME)) {
                String downloadLink = response.getString("download");
                String releaseLink = response.getString("page");

                if (!calledFromNotif) {
                    // Set up notification
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(MainActivity.INTENT_VALUE_DISPLAY_UPDATE, true);
                    PendingIntent pendingIntent = PendingIntent
                            .getActivity(activity, 0, intent, 0);
                    NotificationCompat.Builder notif = new NotificationCompat.Builder
                            (activity, activity.getPackageName())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(activity.getString(R.string.app_name))
                            .setContentText(activity.getString(R.string.a_update_app))
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setLights(Color.BLUE, 2000, 0)
                            .setVibrate(new long[]{0, 250, 250, 250, 250})
                            .setAutoCancel(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notif.setChannelId(activity.getString(R.string.notif_channel_update_ID));
                    }
                    NotificationManagerCompat manager = NotificationManagerCompat.from(activity);
                    manager.notify(activity.getTaskId(), notif.build());
                }

                // Set up dialog
                DialogInterface.OnShowListener updateListener = dialogInterface -> {
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        dialogInterface.dismiss();
                        updateViaGithub(downloadLink);
                    });
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                        Intent githubReleaseSite = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(releaseLink));
                        activity.startActivity(githubReleaseSite);
                    });
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                            view -> dialogInterface.dismiss());
                };
                new AutoDismissDialog(activity.getString(R.string.a_update_app),
                        activity.getString(R.string.a_new_version), new String[]
                        {activity.getString(android.R.string.yes),
                        activity.getString(android.R.string.no),
                        activity.getString(R.string.a_learn_more)}, updateListener)
                        .show(activity.getSupportFragmentManager(), "AppUpdate.1");
            }
        } catch (JSONException e) {
            Log.d("com.unununium.vrclient", "Network Error: Response returned by " + BuildConfig.SERVER_URL
                    + " invalid, response given is " + response + ", error given is "
                    + e.getMessage());
        }
    }

    /** Download and update the newest version of the app via github,
     * separated from showUpdateNotif(JSONArray response) for clarity. **/
    private void updateViaGithub(String downloadLink) {
        // Generate output file name
        // Checks if the /files directory exists, if not it is created
        File filesDir = new File(activity.getFilesDir().getAbsolutePath() + "/temp");
        if (filesDir.exists() || filesDir.mkdir()) {
            // Ask other APK files is deleted on startup, leftover files would not be checked here
            String outputFileName = generateValidFile(activity
                    .getFilesDir().getAbsolutePath() + "/temp/studyassistant-update");
            RequestQueue queue = Volley.newRequestQueue(activity);
            // Boolean used as it is possible for user to cancel the dialog before the download starts
            AtomicBoolean continueDownload = new AtomicBoolean(true);
            ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true);
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> {
                Log.d("com.unununium.vrclient", "Notification: Download of latest APK cancelled");
                queue.stop();
                continueDownload.set(false);
            };
            AutoDismissDialog downloadDialog = new AutoDismissDialog(activity
                    .getString(R.string.a_downloading), progressBar,
                    new String[]{activity.getString(android.R.string.cancel), "", ""});
            downloadDialog.setCancellable(false);
            downloadDialog.setDismissListener(dismissListener);
            downloadDialog.show(activity.getSupportFragmentManager(), "AppUpdate.2");

            VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(Request.Method.GET,
                    downloadLink, response -> {
                try {
                    downloadDialog.dismiss();
                    queue.stop();
                    if (response != null) {
                        File outputFile = new File(outputFileName);
                        if (outputFile.createNewFile()) {
                            SharedPreferences.Editor editor = activity.getSharedPreferences(
                                    activity.getPackageName(), Context.MODE_PRIVATE).edit();
                            editor.putString(MainActivity.SHAREDPREF_APP_UPDATE_PATH, outputFileName);
                            editor.apply();

                            // Write output file with buffer
                            InputStream input = new ByteArrayInputStream(response);
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
                            byte[] data = new byte[1024];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                output.write(data, 0, count);
                            }
                            output.flush();
                            output.close();
                            input.close();

                            // Install app
                            Toast.makeText(activity, R.string.a_app_updating, Toast.LENGTH_SHORT).show();
                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(FileProvider.getUriForFile(activity,
                                    "com.pcchin.studyassistant.provider",
                                    new File(outputFileName)),
                                    "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(installIntent);
                        } else {
                            Log.d("com.unununium.vrclient", "File Error: File " + outputFileName
                                    + " could not be created.");
                            Toast.makeText(activity, R.string.a_file_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.d("com.unununium.vrclient", "File Error: File" + outputFileName + " not found, stack trace is ");
                    e.printStackTrace();
                    Toast.makeText(activity, R.string.a_file_error, Toast.LENGTH_SHORT).show();
                } catch (IOException e2) {
                    Log.d("com.unununium.vrclient", "File Error: An IOException occurred at " + outputFileName
                            + ", stack trace is");
                    e2.printStackTrace();
                    Toast.makeText(activity, R.string.a_file_error, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                Log.d("com.unununium.vrclient", "Error: Volley download request failed " +
                        "in middle of operation with error");
                e.printStackTrace();
                Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                downloadDialog.dismiss();
                Log.d("com.unununium.vrclient", "Network Error: Volley file download request failed"
                        + ", response given is " + error.getMessage() + ", stack trace is");
                error.printStackTrace();
                Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }, null){
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("User-agent", USER_AGENT);
                    return headers;
                }
            };

            if (continueDownload.get()) {
                queue.add(request);
            }
        }
    }

    /** Generates a valid file in the required directory.
     * If a file with the same name exists,
     * a file with incrementing number will be added to the file.  **/
    private static String generateValidFile(String filename) {
        String returnFile = filename + ".apk";
        int i = 1;
        while (new File(returnFile).exists() && i < Integer.MAX_VALUE) {
            returnFile = filename + "(" + i + ")" + ".apk";
            i++;
        }
        return returnFile;
    }
}
