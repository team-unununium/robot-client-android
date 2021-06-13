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

import io.github.unununium.activity.MainActivity;
import io.github.unununium.comm.ConnectionParameters;

/** An extension to MainActivity that handles changes in values in connection parameters. **/
public class ValueHandler {
    private final MainActivity parent;
    private final ConnectionParameters params;

    public ValueHandler(MainActivity parent, ConnectionParameters params) {
        this.parent = parent;
        this.params = params;
    }

    public void onDataReceived() {
        // TODO: Complete
    }

    public void onStateChanged() {
        // TODO: Complete
    }

    public void onOperatorChanged() {
        // TODO: Complete
    }

    public void onVelocityChanged() {
        // TODO: Complete
    }

    public void onMovementChanged() {
        // TODO: Complete
    }

    public void onCameraRotationChanged() {
        // TODO: Complete
    }

    public void onRotationChanged() {
        // TODO: Complete
    }
}
