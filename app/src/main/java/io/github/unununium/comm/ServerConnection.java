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

import com.android.volley.AuthFailureError;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import io.github.unununium.BuildConfig;
import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;
import io.github.unununium.util.CameraSurfaceView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/** The Socket.IO connection between the server and the phone. **/
public class ServerConnection {
    private MainActivity parent;
    private String accessToken = null;
    private Socket socket = null;
    private NetworkStateListener listener;
    private final DecimalFormat fourDP = new DecimalFormat("0.0000");

    private final Emitter.Listener onSessionInfoReceived = args -> {
        parseData((JSONObject) args[0]);
        parent.valueHandler.onDataReceived();
    };

    private final Emitter.Listener onTestClientReceived = args -> {
        if (parent.remoteParams.isOperator) {
            onConnectionFailure("testClient event received for operator");
        } else {
            setState(ConnectionParameters.State.CONNECTED);
        }
    };

    private final Emitter.Listener onTestOperatorReceived = args -> {
        if (parent.remoteParams.isOperator) {
            setState(ConnectionParameters.State.CONNECTED);
        } else {
            onConnectionFailure("testOperator received for client");
        }
    };

    private final Emitter.Listener onTestRobotReceived = args ->
            onConnectionFailure("testRobot received for client");

    private final Emitter.Listener onVideoBufferReceived = args ->
            parent.runOnUiThread(() -> ((CameraSurfaceView)
                    parent.findViewById(R.id.m1_playerview)).setCurrentImage((byte[]) args[0]));

    public ServerConnection(MainActivity parent) {
        this.parent = parent;
        if (isOnline()) setState(ConnectionParameters.State.DISCONNECTED);
        else setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);
    }

    public void createConnection() {
        if (parent.remoteParams.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            Toast.makeText(parent, parent.remoteParams.getStateString(), Toast.LENGTH_SHORT).show();
            return;
        }

        parent.remoteParams.guid = UUID.randomUUID().toString();
        // Attempt to get a access token
        RequestQueue queue = Volley.newRequestQueue(parent);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("guid", parent.remoteParams.guid);
            requestBody.put("secret", parent.remoteParams.isOperator ?
                    BuildConfig.SERVER_OPERATOR_SECRET : BuildConfig.SERVER_CLIENT_SECRET);
            String requestString = requestBody.toString();
            JsonObjectRequest accessRequest = getAccessRequest(requestString);
            queue.add(accessRequest);
            setState(ConnectionParameters.State.ACQUIRING_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(parent, R.string.error_network, Toast.LENGTH_SHORT).show();
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
                options.extraHeaders.put("guid", Collections.singletonList(parent.remoteParams.guid));
                options.extraHeaders.put("token", Collections.singletonList(accessToken));
                socket = IO.socket(BuildConfig.SERVER_URL, options);
                setState(ConnectionParameters.State.SOCKET_DISCONNECTED);
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
        if (socket != null && !socket.connected()) {
            setState(ConnectionParameters.State.STARTING_SOCKET);
            socket.on("clientUpdateData", onSessionInfoReceived);
            socket.on("testClient", onTestClientReceived);
            socket.on("testOperator", onTestOperatorReceived);
            socket.on("testRobot", onTestRobotReceived);
            socket.on("clientSendVideo", onVideoBufferReceived);
            socket.connect();
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        listener = new NetworkStateListener(this);
        parent.registerReceiver(listener, filter);
    }

    public void pauseConnection() {
        if (socket != null && socket.isActive() && socket.connected()) {
            socket.disconnect();
            socket.off("clientUpdateData", onSessionInfoReceived);
            socket.off("testClient", onTestClientReceived);
            socket.off("testOperator", onTestOperatorReceived);
            socket.off("testRobot", onTestRobotReceived);
            socket.off("clientSendVideo", onVideoBufferReceived);
            setState(ConnectionParameters.State.SOCKET_DISCONNECTED);
        }
        try {
            if (listener != null) parent.unregisterReceiver(listener);
        } catch (IllegalArgumentException e) {
            listener = null;
        }
    }

    public void terminateConnection() {
        if (parent.remoteParams.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            Toast.makeText(parent, parent.remoteParams.getStateString(), Toast.LENGTH_SHORT).show();
            return;
        }
        pauseConnection();
        RequestQueue queue = Volley.newRequestQueue(parent);
        StringRequest deleteRequest = getDeleteRequest(parent.remoteParams.guid, accessToken);
        queue.add(deleteRequest);
    }

    @NotNull
    private StringRequest getDeleteRequest(String guid, String accessToken) {
        String accessUrl = BuildConfig.SERVER_URL + "/access";
        return new StringRequest(Request.Method.DELETE, accessUrl, response -> {
            if (Objects.equals(response, "")) {
                setState(ConnectionParameters.State.DISCONNECTING);
            } else {
                onConnectionFailure("Response " + response + " received from server despite 204 expected");
            }
        }, error -> {
            if (error.networkResponse.statusCode == 204 || error.networkResponse.statusCode == 404) {
                setState(ConnectionParameters.State.DISCONNECTING);
            } else {
                onConnectionFailure(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // https://stackoverflow.com/questions/22803766/volley-how-to-send-delete-request-parameters
                // DELETE never sends body in Volley, so headers had to be used instead
                Map<String, String> headers = super.getHeaders();

                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }

                headers.put("guid", guid);
                headers.put("token", accessToken);

                return headers;
            }
        };
    }

    private void onConnectionFailure(String message) {
        Log.w("ServerConnection", "Network connection failed with message " + message);
        if (isOnline()) setState(ConnectionParameters.State.DISCONNECTED);
        else setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);

        // Try without operator
        if (parent.remoteParams.isOperator) {
            setOperator(false);
            Toast.makeText(parent, R.string.revert_from_operator, Toast.LENGTH_SHORT).show();
            createConnection();
        }
    }

    public void onNetworkStateChanged(boolean isConnected) {
        if (isConnected && parent.remoteParams.state == ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            setState(ConnectionParameters.State.DISCONNECTED);
            createConnection();
        } else if (!isConnected && parent.remoteParams.state != ConnectionParameters.State.NETWORK_NOT_AVAILABLE) {
            if (parent.remoteParams.state != ConnectionParameters.State.DISCONNECTED) {
                socket = null;
                accessToken = null;
                Toast.makeText(parent, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
            setState(ConnectionParameters.State.NETWORK_NOT_AVAILABLE);
        }
    }

    /** Parses the data that is received from the robot into its individual values **/
    public void parseData(@NotNull JSONObject object) {
        // Individual try statements allow for incomplete objects to be parsed
        try {
            parent.remoteParams.temperature = object.getDouble("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.humidity = object.getDouble("humidity");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.frontObstacle = object.getDouble("frontObstacle");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.backObstacle = object.getDouble("backObstacle");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.co = object.getDouble("co");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.ch4 = object.getDouble("ch4");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.h2 = object.getDouble("h2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            parent.remoteParams.lpg = object.getDouble("lpg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //****** Start of setters ******

    private void setState(ConnectionParameters.State state) {
        parent.remoteParams.state = state;
        parent.valueHandler.onStateChanged();
    }

    public void setOperator(boolean isOperator) {
        parent.remoteParams.isOperator = isOperator;
        terminateConnection();
        createConnection();
        parent.valueHandler.onOperatorChanged();
    }

    public void setVelocity(int velocity) {
        parent.remoteParams.velocity = velocity;
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", velocity);
            if (socket != null) socket.emit("operatorChangeSpeed", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parent.valueHandler.onVelocityChanged();
    }

    public void setMoving(boolean moving) {
        parent.remoteParams.isMoving = moving;
        if (socket != null) {
            if (moving) {
                socket.emit("operatorStartMoving");
            } else {
                socket.emit("operatorStopMoving");
            }
        }
        parent.valueHandler.onMovementChanged();
    }

    public void setCameraRotation(float cameraRotation) {
        parent.remoteParams.cameraRotation = cameraRotation;
        // Trim to 4dp, converted to String before sending over
        String rotationString = fourDP.format(cameraRotation);
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", rotationString);
            if (socket != null) socket.emit("operatorRotateCamera", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parent.valueHandler.onCameraRotationChanged();
    }

    public void setRobotRotation(float robotRotation) {
        parent.remoteParams.robotRotation = robotRotation;
        // Trim to 4dp, converted to String before sending over
        String rotationString = fourDP.format(robotRotation);
        JSONObject obj = new JSONObject();
        try {
            obj.put("velocity", rotationString);
            if (socket != null) socket.emit("operatorRotate", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parent.valueHandler.onRotationChanged();
    }
}
