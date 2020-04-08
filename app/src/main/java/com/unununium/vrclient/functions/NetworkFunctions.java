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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.unununium.vrclient.BuildConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Functions that require network connection. **/
public class NetworkFunctions {
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
}
