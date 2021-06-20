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

import java.util.UUID;

import io.github.unununium.R;

/** The parameters that are currently being used in the app to connect with the server. **/
public class ConnectionParameters {
    public enum State {
        DISCONNECTED,
        ACQUIRING_TOKEN,
        STARTING_SOCKET,
        CONNECTED,
        DISCONNECTING,
        SOCKET_DISCONNECTED,
        NETWORK_NOT_AVAILABLE
    }

    State state = State.DISCONNECTED;
    boolean isOperator = false;
    final String guid = UUID.randomUUID().toString();
    double temperature = 0;
    double humidity = 0;
    double frontObstacle = -1;
    double backObstacle = -1;
    double co = 0;
    double ch4 = 0;
    double h2 = 0;
    double lpg = 0;
    double bufferDuration = 0.4;
    int videoWidth = 1280;
    int videoHeight = 720;
    int velocity = 1;
    boolean isMoving = false;
    float cameraRotation = 0f;
    float robotRotation = 0f;

    //****** Start of getter functions ******//

    public State getState() {
        return state;
    }

    public int getStateString() {
        switch (state) {
            case DISCONNECTED:
                return R.string.state_disconnected;
            case ACQUIRING_TOKEN:
                return R.string.state_acquiring_token;
            case STARTING_SOCKET:
                return R.string.state_starting_socket;
            case CONNECTED:
                return R.string.state_connected;
            case SOCKET_DISCONNECTED:
                return R.string.state_socket_disconnected;
            case DISCONNECTING:
                return R.string.state_disconnecting;
            case NETWORK_NOT_AVAILABLE:
                return R.string.state_network_not_available;
            default:
                return R.string.disconnected;
        }
    }

    public boolean isOperator() {
        return isOperator;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getFrontObstacle() {
        return frontObstacle;
    }

    public double getBackObstacle() {
        return backObstacle;
    }

    public double getCo() {
        return co;
    }

    public double getCh4() {
        return ch4;
    }

    public double getLpg() {
        return lpg;
    }

    public double getH2() {
        return h2;
    }

    public int getVelocity() {
        return velocity;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public float getCameraRotation() {
        return cameraRotation;
    }

    public float getRobotRotation() {
        return robotRotation;
    }
}
