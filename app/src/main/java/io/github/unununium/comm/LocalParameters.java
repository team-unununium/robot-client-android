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

import java.text.DecimalFormat;

import io.github.unununium.util.Constants;

/** The parameters that are currently being used in the app for local updates. **/
public class LocalParameters {
    public enum ControlMode {
        DISABLED,
        CAMERA,
        ROBOT
    }

    public final DecimalFormat fourDP = new DecimalFormat("0.0000");
    public final DecimalFormat twoDP = new DecimalFormat("0.00");
    public final DecimalFormat oneDP = new DecimalFormat("0.0");

    public boolean externalControllerEnabled = false;
    public ControlMode phoneControlMode = ControlMode.CAMERA;

    public boolean normalOverlayIsText = false;
    public boolean diagnosticsModeEnabled = false;
    public boolean uiIsHidden = false;
    public boolean upperOverlayIsHidden = true;
    public boolean isDay = true;

    public float lowerTempBound = 20.0f;
    public float upperTempBound = 60.0f;
    public float lowerHumidityBound = 0.40f;
    public float upperHumidityBound = 0.88f;
    public float frontObstacleAmount = 200f;
    public float rearObstacleAmount = 200f;

    public float coWarnLevel = 0f;
    public float ch4WarnLevel = 0f;
    public float h2WarnLevel = 0f;
    public float lpgWarnLevel = 0f;

    public int getControlModeInt() {
        return phoneControlMode == LocalParameters.ControlMode.DISABLED ? 0
                : phoneControlMode == LocalParameters.ControlMode.CAMERA ? 1 : 2;
    }

    public Constants.OverlayType getCurrentOverlay() {
        if (!uiIsHidden) {
            if (upperOverlayIsHidden) {
                if (diagnosticsModeEnabled) return Constants.OverlayType.TYPE_DIAGNOSTICS;
                else if (normalOverlayIsText) return Constants.OverlayType.TYPE_NORMAL_TEXT;
                else return Constants.OverlayType.TYPE_NORMAL_ICON;
            } else {
                return Constants.OverlayType.TYPE_SETTINGS;
            }
        } else {
            return Constants.OverlayType.TYPE_NONE;
        }
    }
}
