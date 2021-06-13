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

package io.github.unununium.comm;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;

import io.github.unununium.BuildConfig;
import io.github.unununium.R;
import io.github.unununium.util.ValueHandler;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/** The Socket.IO connection between the server and the phone. **/
public class ServerConnection {
    private final Context context;
    private ValueHandler valueHandler;
    private String accessToken = null;
    private ConnectionParameters params;
    private Socket socket = null;
    private NetworkStateListener listener;
    private final DecimalFormat fourDP = new DecimalFormat("0.0000");

    private final Emitter.Listener onSessionInfoReceived = args -> {
        parseData((JSONObject) args[0]);
        valueHandler.onDataReceived();
    };

    private final Emitter.Listener onTestClientReceived = args -> {
        if (params.isOperator) {
            onConnectionFailure("testClient event received for operator");
        } else {
            setState(ConnectionParameters.State.CONNECTED);
        }
    };

    private final Emitter.Listener onTestOperatorReceived = args -> {
        if (params.isOperator) {
            setState(ConnectionParameters.State.CONNECTED);
        } else {
            onConnectionFailure("testOperator received for client");
        }
    };

    public ServerConnection(Context context, ValueHandler handler) {
        this.context = context;
        this.params = new ConnectionParameters();
        this.valueHandler = handler;
        if (isOnline()) setState(ConnectionParameters.State.DISCONNECTED);
        else setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);
    }

    public void createConnection() {
        if (params.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            Toast.makeText(context, params.getStateString(), Toast.LENGTH_SHORT).show();
            return;
        }
        // Attempt to get a access token
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("guid", params.guid);
            requestBody.put("secret", params.isOperator ?
                    BuildConfig.SERVER_OPERATOR_SECRET : BuildConfig.SERVER_CLIENT_SECRET);
            String requestString = requestBody.toString();
            JsonObjectRequest accessRequest = getAccessRequest(requestString);
            queue.add(accessRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show();
        }
    }

    @NotNull
    @Contract("_ -> new")
    private JsonObjectRequest getAccessRequest(String requestString) {
        String accessUrl = BuildConfig.SERVER_URL + "/access";
        return new JsonObjectRequest(Request.Method.POST, accessUrl, null, response -> {
            try {
                accessToken = response.getString("token");
                IO.Options options = new IO.Options();
                options.extraHeaders = new HashMap<>();
                options.extraHeaders.put("guid", Collections.singletonList(params.guid));
                options.extraHeaders.put("token", Collections.singletonList(accessToken));
                socket = IO.socket(BuildConfig.SERVER_URL, options);
                resumeConnection();
            } catch (JSONException | URISyntaxException e) {
                onConnectionFailure(e.getMessage());
            }
        }, error -> onConnectionFailure(error.getMessage())) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestString == null ? null : requestString.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    public void resumeConnection() {
        if (socket != null && socket.isActive() && !socket.connected()) {
            socket.on("robotSendSessionInfo", onSessionInfoReceived);
            socket.on("testClient", onTestClientReceived);
            socket.on("testOperator", onTestOperatorReceived);
            socket.connect();
            setState(ConnectionParameters.State.STARTING_SOCKET);
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        listener = new NetworkStateListener(this);
        context.registerReceiver(listener, filter);
    }

    public void pauseConnection() {
        if (socket != null && socket.isActive() && socket.connected()) {
            socket.disconnect();
            socket.off("robotSendSessionInfo", onSessionInfoReceived);
            socket.off("testClient", onTestClientReceived);
            socket.off("testOperator", onTestOperatorReceived);
            setState(ConnectionParameters.State.SOCKET_DISCONNECTED);
        }
        if (listener != null) context.unregisterReceiver(listener);
    }

    public void terminateConnection() {
        if (params.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            Toast.makeText(context, params.getStateString(), Toast.LENGTH_SHORT).show();
            return;
        }
        pauseConnection();
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("guid", params.guid);
            requestBody.put("token", accessToken);
            String requestString = requestBody.toString();
            StringRequest deleteRequest = getDeleteRequest(requestString);
            queue.add(deleteRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show();
        }
    }

    @NotNull
    private StringRequest getDeleteRequest(String requestString) {
        String accessUrl = BuildConfig.SERVER_URL + "/access";
        return new StringRequest(Request.Method.DELETE, accessUrl, response -> {
            // THIS IS NOT SUPPOSED TO HAPPEN
            // EVERYONE PANIC
            onConnectionFailure("Response " + response + " received from server despite 204 expected");
        }, error -> {
            if (error.networkResponse.statusCode == 204) {
                setState(ConnectionParameters.State.DISCONNECTING);
            } else {
                onConnectionFailure(error.getMessage());
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return requestString == null ? null : requestString.getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    private void onConnectionFailure(String message) {
        Log.w("ServerConnection", "Network connection failed with message " + message);
        if (isOnline()) setState(ConnectionParameters.State.DISCONNECTED);
        else setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);

        // Try without operator
        if (params.isOperator) {
            setOperator(false);
            Toast.makeText(context, R.string.revert_from_operator, Toast.LENGTH_SHORT).show();
            createConnection();
        }
    }

    public void onNetworkStateChanged(boolean isConnected) {
        if (isConnected && params.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            setState(ConnectionParameters.State.DISCONNECTED);
            createConnection();
        } else if (!isConnected && params.state != ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            if (params.state != ConnectionParameters.State.DISCONNECTED) {
                socket = null;
                accessToken = null;
                Toast.makeText(context, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
            setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);
        }
    }

    /** Parses the data that is received from the robot into its individual values **/
    public void parseData(@NotNull JSONObject object) {
        // Individual try statements allow for incomplete objects to be parsed
        try {
            params.temperature = object.getDouble("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.humidity = object.getDouble("humidity");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.frontObstacle = object.getDouble("frontObstacle");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.backObstacle = object.getDouble("backObstacle");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.co = object.getDouble("co");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.ch4 = object.getDouble("ch4");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.h2 = object.getDouble("h2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.lpg = object.getDouble("lpg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //****** Start of setters ******

    private void setState(ConnectionParameters.State state) {
        params.state = state;
        valueHandler.onStateChanged();
    }

    public void setOperator(boolean isOperator) {
        params.isOperator = isOperator;
        valueHandler.onOperatorChanged();
    }

    public void setVelocity(int velocity) {
        params.velocity = velocity;
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", velocity);
            if (socket != null) socket.emit("robotChangeSpeed", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        valueHandler.onVelocityChanged();
    }

    public void setMoving(boolean moving) {
        params.isMoving = moving;
        if (moving) {
            socket.emit("startMoving");
        } else {
            socket.emit("stopMoving");
        }
        valueHandler.onMovementChanged();
    }

    public void setCameraRotation(float cameraRotation) {
        params.cameraRotation = cameraRotation;
        // Trim to 4dp, converted to String before sending over
        String rotationString = fourDP.format(cameraRotation);
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", rotationString);
            if (socket != null) socket.emit("robotRotateCamera", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        valueHandler.onCameraRotationChanged();
    }

    public void setRobotRotation(float robotRotation) {
        params.robotRotation = robotRotation;
        // Trim to 4dp, converted to String before sending over
        String rotationString = fourDP.format(robotRotation);
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", rotationString);
            if (socket != null) socket.emit("robotRotate", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        valueHandler.onRotationChanged();
    }
}
