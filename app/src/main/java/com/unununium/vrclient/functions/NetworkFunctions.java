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

package com.unununium.vrclient.functions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.unununium.vrclient.BuildConfig;
import com.unununium.vrclient.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Functions that require network connection. **/
public class NetworkFunctions {
    // Drawable lists
    private static int[] TEMP_LIST = new int[]{R.drawable.ic_temp_cold,
            R.drawable.ic_temp_default, R.drawable.ic_temp_hot};
    private static int[] GAS_LIST = new int[]{R.drawable.ic_gas_co, R.drawable.ic_gas_ch4,
            R.drawable.ic_gas_h2, R.drawable.ic_gas_lpg};
    private static int[] HUMIDITY_LIST = new int[]{R.drawable.ic_humidity_low,
            R.drawable.ic_humidity_medium, R.drawable.ic_humidity_high};
    
    /** Check if network is connected and that the server is working. **/
    public static boolean checkServerOnline(@NotNull Context context) {
        boolean isConnected = false;
        OkHttpClient client = new OkHttpClient();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        if (!isConnected) {
            return false;
        }

        Request request = new Request.Builder().url(BuildConfig.SERVER_URL + "/test").build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            return response.code() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    /** Returns a token from the server based on the GUID and secret provided.
     * This function should not be called on the main thread. **/
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static String requestToken(String secret, @NotNull SharedPreferences sharedPref,
                                      @NotNull OkHttpClient client) {
        try {
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("guid", sharedPref.getString("guid", ""));
            bodyObj.put("secret", secret);
            RequestBody body = RequestBody.Companion.create(bodyObj.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(BuildConfig.SERVER_URL + "/access")
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() == 201 && response.body() != null) {
                try {
                    String token = new JSONObject(response.body().string()).getString("token");
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("token", token);
                    editor.apply();
                    return token;
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                Log.w("VR Client", "Network Error: Response of POST request from server returns code " + response.code());
                return null;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Returns a boolean based on whether the token is successfully deleted.
     * This function should not be called on the main thread. **/
    public static boolean deleteToken(@NotNull SharedPreferences sharedPref, @NotNull OkHttpClient client) {
        JSONObject bodyObj = new JSONObject();
        try {
            bodyObj.put("guid", sharedPref.getString("guid", ""));
            bodyObj.put("token", sharedPref.getString("token", ""));
            RequestBody body = RequestBody.Companion.create(bodyObj.toString(),
                    MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(BuildConfig.SERVER_URL + "/access")
                    .delete(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() == 204) {
                return true;
            } else {
                Log.w("VR Client", "Network Error: Response of DELETE request from server returns code " + response.code());
                return false;
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sets up the listener for the sockets and starts the connection to the server.
     * This function should not be run on the main thread. **/
    @Nullable
    public static Socket initSocket(@NotNull Activity activity, ImageView tempImg,
                                    ImageView gasImg, ImageView humidityImg, ImageView frontObstacleImg,
                                    ImageView obstacleImg, ImageView backObstacleImg) {
        // Check token
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getPackageName(), Context.MODE_PRIVATE);
        String guid = sharedPref.getString("guid", ""),
                token = sharedPref.getString("token", "");
        // Connect to socket.io with token
        try {
            IO.Options options = new IO.Options();
            options.query = "client";
            Socket socket = IO.socket(BuildConfig.SERVER_URL, options);
            socket.on(Socket.EVENT_CONNECT, args -> {
                HashMap<String, String> jsonMap = new HashMap<>();
                jsonMap.put("guid", guid);
                jsonMap.put("token", token);
                socket.emit("authentication", new JSONObject(jsonMap).toString());
            })
                    .on("authorized",
                    args -> socket.emit("clientRequestData"))
                    .on("unauthorized", args ->
                            activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(),
                                    "Unauthorized",
                                    Toast.LENGTH_SHORT).show()))
                    .on("clientDataReceived", args -> {
                        try {
                            JSONObject object = new JSONObject(args[0].toString());
                            // Get updated values
                            int temp = object.getInt("temp"),
                                    gasType = object.getInt("gas"),
                                    humidity = object.getInt("humidity");
                            boolean frontObstacle = object.getBoolean("frontObstacle"),
                                    backObstacle = object.getBoolean("backObstacle");

                            // Update values to show on screen
                            activity.runOnUiThread(() -> {
                                tempImg.setImageDrawable(activity.getDrawable(TEMP_LIST[temp]));
                                if (gasType == 0) {
                                    gasImg.setImageDrawable(activity.getDrawable(android.R.color.transparent));
                                } else {
                                    gasImg.setImageDrawable(activity.getDrawable(GAS_LIST[gasType - 1]));
                                }
                                humidityImg.setImageDrawable(activity.getDrawable(HUMIDITY_LIST[humidity + 1]));
                                if (!frontObstacle && !backObstacle) {
                                    frontObstacleImg .setImageDrawable(activity.getDrawable(android.R.color.transparent));
                                    obstacleImg.setImageDrawable(activity.getDrawable(android.R.color.transparent));
                                    backObstacleImg.setImageDrawable(activity.getDrawable(android.R.color.transparent));
                                } else {
                                    obstacleImg.setImageDrawable(activity.getDrawable(R.drawable.ic_obstacle));
                                    if (frontObstacle) {
                                        frontObstacleImg.setImageDrawable(activity.getDrawable(R.drawable.ic_obstacle_front));
                                    }
                                    if (backObstacle) {
                                        backObstacleImg.setImageDrawable(activity.getDrawable(R.drawable.ic_obstacle_back));
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            Toast.makeText(activity.getApplicationContext(),
                                    activity.getString(R.string.a_network_error), Toast.LENGTH_SHORT).show();
                        }
                    });
            socket.connect();
            return socket;
        } catch (URISyntaxException e) {
            Log.w(activity.getString(R.string.app_name), activity.getString(R.string.a_network_error));
            e.printStackTrace();
            return null;
        }
    }
}
