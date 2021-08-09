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

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;
import io.github.unununium.comm.ConnectionParameters;
import io.github.unununium.fragment.OverlayFragment;

/** An extension to MainActivity that handles changes in values in connection parameters. **/
public class ValueHandler {
    private final MainActivity parent;

    public ValueHandler(MainActivity parent) {
        this.parent = parent;
    }

    public void onDataReceived() {
        parent.runOnUiThread(() -> {
            switch (parent.localParams.getCurrentOverlay()) {
                case TYPE_DIAGNOSTICS:
                    refreshDiagnosticsPage();
                    break;
                case TYPE_NORMAL_ICON:
                    refreshNormalIconPage();
                    break;
                case TYPE_NORMAL_TEXT:
                    refreshNormalTextPage();
                    break;
                case TYPE_SETTINGS:
                case TYPE_NONE:
                    break;
            }
        });
    }

    public void refreshDiagnosticsPage() {
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_temp))
                .setText(String.format("%s °C", parent.localParams.oneDP.format(parent.remoteParams.getTemperature())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_humidity))
                .setText(String.format("%s %%", parent.localParams.oneDP.format(parent.remoteParams.getHumidity() % 100)));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_front_obstacle))
                .setText(String.format("Front: %s mm", parent.localParams.twoDP.format(parent.remoteParams.getFrontObstacle())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_back_obstacle))
                .setText(String.format("Back: %s mm", parent.localParams.twoDP.format(parent.remoteParams.getBackObstacle())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_co_level))
                .setText(String.format("CO: %s", parent.localParams.fourDP.format(parent.remoteParams.getCo())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_ch4_level))
                .setText(String.format("CH4: %s", parent.localParams.fourDP.format(parent.remoteParams.getCh4())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_h2_level))
                .setText(String.format("H2: %s", parent.localParams.fourDP.format(parent.remoteParams.getH2())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_lpg_level))
                .setText(String.format("LPG: %s", parent.localParams.fourDP.format(parent.remoteParams.getLpg())));
    }

    public void refreshNormalIconPage() {
        int tempResIcon = parent.localParams.isDay ?
                (parent.remoteParams.getTemperature() < parent.localParams.lowerTempBound ?
                        R.drawable.ic_temp_cold_day : parent.remoteParams.getTemperature() >
                        parent.localParams.upperTempBound ? R.drawable.ic_temp_hot_day :
                        R.drawable.ic_temp_default_day) :
                (parent.remoteParams.getTemperature() < parent.localParams.lowerTempBound ?
                R.drawable.ic_temp_cold_night : parent.remoteParams.getTemperature() >
                parent.localParams.upperTempBound ? R.drawable.ic_temp_hot_night :
                R.drawable.ic_temp_default_night);
        ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_temp_icon)).setImageResource(tempResIcon);
        int humidityResIcon = parent.localParams.isDay ?
                (parent.remoteParams.getHumidity() < parent.localParams.lowerHumidityBound ?
                        R.drawable.ic_humidity_low_day : parent.remoteParams.getHumidity() >
                        parent.localParams.upperHumidityBound ? R.drawable.ic_humidity_high_day :
                        R.drawable.ic_humidity_medium_day) :
                (parent.remoteParams.getHumidity() < parent.localParams.lowerHumidityBound ?
                        R.drawable.ic_humidity_low_night : parent.remoteParams.getHumidity() >
                        parent.localParams.upperHumidityBound ? R.drawable.ic_humidity_high_night :
                        R.drawable.ic_humidity_medium_night);
        ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_humidity_icon)).setImageResource(humidityResIcon);

        parent.currentFragment.requireView().findViewById(R.id.overlay_obstacle_front_icon)
                .setVisibility(parent.remoteParams.getFrontObstacle() < parent.localParams.frontObstacleAmount ?
                        View.VISIBLE : View.GONE);
        parent.currentFragment.requireView().findViewById(R.id.overlay_obstacle_back_icon)
                .setVisibility(parent.remoteParams.getBackObstacle() < parent.localParams.rearObstacleAmount ?
                        View.VISIBLE : View.GONE);
        parent.currentFragment.requireView().findViewById(R.id.overlay_obstacle_icon)
                .setVisibility((parent.remoteParams.getFrontObstacle() < parent.localParams.frontObstacleAmount
                        || parent.remoteParams.getBackObstacle() < parent.localParams.rearObstacleAmount) ?
                        View.VISIBLE : View.GONE);

        boolean shouldShowCoIcon = parent.remoteParams.getCo() > parent.localParams.coWarnLevel;
        parent.currentFragment.requireView().findViewById(R.id.overlay_co_icon)
                .setVisibility(shouldShowCoIcon ? View.VISIBLE : View.GONE);
        if (shouldShowCoIcon) ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_co_icon))
                .setImageResource(parent.localParams.isDay ? R.drawable.ic_gas_co_day : R.drawable.ic_gas_co_night);

        boolean shouldShowCh4Icon = parent.remoteParams.getCh4() > parent.localParams.ch4WarnLevel;
        parent.currentFragment.requireView().findViewById(R.id.overlay_ch4_icon)
                .setVisibility(shouldShowCh4Icon ? View.VISIBLE : View.GONE);
        if (shouldShowCh4Icon) ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_ch4_icon))
                .setImageResource(parent.localParams.isDay ? R.drawable.ic_gas_ch4_day : R.drawable.ic_gas_ch4_night);

        boolean shouldShowH2Icon = parent.remoteParams.getH2() > parent.localParams.h2WarnLevel;
        parent.currentFragment.requireView().findViewById(R.id.overlay_h2_icon)
                .setVisibility(shouldShowH2Icon ? View.VISIBLE : View.GONE);
        if (shouldShowH2Icon) ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_h2_icon))
                .setImageResource(parent.localParams.isDay ? R.drawable.ic_gas_h2_day : R.drawable.ic_gas_h2_night);

        boolean shouldShowLpgIcon = parent.remoteParams.getLpg() > parent.localParams.lpgWarnLevel;
        parent.currentFragment.requireView().findViewById(R.id.overlay_lpg_icon)
                .setVisibility(shouldShowCoIcon ? View.VISIBLE : View.GONE);
        if (shouldShowLpgIcon) ((ImageView) parent.currentFragment.requireView().findViewById(R.id.overlay_lpg_icon))
                .setImageResource(parent.localParams.isDay ? R.drawable.ic_gas_lpg_day : R.drawable.ic_gas_lpg_night);
    }

    public void refreshNormalTextPage() {
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_temp_text))
                .setText(String.format("%s °C", parent.localParams.oneDP.format(parent.remoteParams.getTemperature())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_humidity_text))
                .setText(String.format("%s %%", parent.localParams.oneDP.format(parent.remoteParams.getHumidity() % 100)));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_front_obstacle_text))
                .setText(String.format("Front: %s mm", parent.localParams.twoDP.format(parent.remoteParams.getFrontObstacle())));
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_back_obstacle_text))
                .setText(String.format("Back: %s mm", parent.localParams.twoDP.format(parent.remoteParams.getBackObstacle())));
        String gasString = "";
        if (parent.remoteParams.getCo() > parent.localParams.coWarnLevel) gasString += "CO, ";
        if (parent.remoteParams.getCh4() > parent.localParams.ch4WarnLevel) gasString += "CH4, ";
        if (parent.remoteParams.getH2() > parent.localParams.h2WarnLevel) gasString += "H2, ";
        if (parent.remoteParams.getLpg() > parent.localParams.lpgWarnLevel) gasString += "LPG";
        if (gasString.endsWith(", ")) gasString = gasString.substring(0, gasString.length() - 3);
        ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_gas_text)).setText(gasString);
    }

    public void onStateChanged() {
        parent.runOnUiThread(() -> {
            ConnectionParameters.State currentState = parent.remoteParams.getState();
            ImageView targetView;
            switch (parent.localParams.getCurrentOverlay()) {
                case TYPE_DIAGNOSTICS:
                    ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_server)).setText(
                            String.format("Server: %s", parent.getString(parent.remoteParams.getStateString())));
                    break;
                case TYPE_NORMAL_ICON:
                    targetView = parent.currentFragment.requireView().findViewById(R.id.overlay_icon_disconnected);
                    targetView.setVisibility(currentState == ConnectionParameters.State.CONNECTED ? View.INVISIBLE : View.GONE);
                    if (currentState != ConnectionParameters.State.CONNECTED) blinkImage(targetView);
                    else targetView.clearAnimation();
                    break;
                case TYPE_NORMAL_TEXT:
                    targetView = parent.currentFragment.requireView().findViewById(R.id.overlay_text_disconnected);
                    targetView.setVisibility(currentState == ConnectionParameters.State.CONNECTED ? View.INVISIBLE : View.GONE);
                    if (currentState != ConnectionParameters.State.CONNECTED) blinkImage(targetView);
                    else targetView.clearAnimation();
                    break;
                default:
                    break;
            }
        });
    }

    public void onOperatorChanged() {
        parent.runOnUiThread(() -> {
            switch (parent.localParams.getCurrentOverlay()) {
                case TYPE_DIAGNOSTICS:
                    ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_app_mode)).setText(
                            String.format("Mode: %s", parent.remoteParams.isOperator() ? "Operator" : "Observer"));
                    ((OverlayFragment) parent.currentFragment).setOperatorViewsVisibility(null, parent.remoteParams.isOperator());
                    break;
                case TYPE_NORMAL_ICON:
                    parent.currentFragment.requireView().findViewById(R.id.overlay_icon_moving).setVisibility(parent.remoteParams.isOperator() ? View.VISIBLE : View.INVISIBLE);
                    break;
                case TYPE_NORMAL_TEXT:
                    parent.currentFragment.requireView().findViewById(R.id.overlay_text_moving).setVisibility(parent.remoteParams.isOperator() ? View.VISIBLE : View.INVISIBLE);
                    break;
                case TYPE_SETTINGS:
                case TYPE_NONE:
                    break;
            }
        });
    }

    public void onVelocityChanged() {
        parent.runOnUiThread(() -> {
            if (parent.localParams.getCurrentOverlay() == Constants.OverlayType.TYPE_DIAGNOSTICS && parent.remoteParams.isMoving()) {
                ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_velocity)).setText(
                        String.format(Locale.ENGLISH, "Velocity: %d", parent.remoteParams.getVelocity()));
            }
        });
    }

    public void onMovementChanged() {
        parent.runOnUiThread(() -> {
            if (parent.currentFragment != null && parent.currentFragment.getView() != null) {
                switch (parent.localParams.getCurrentOverlay()) {
                    case TYPE_DIAGNOSTICS:
                        if (!parent.remoteParams.isMoving()) ((TextView) parent
                                .findViewById(R.id.overlay_diag_velocity)).setText(R.string.velocity_stopped);
                        else onVelocityChanged();
                        break;
                    case TYPE_NORMAL_ICON:
                        parent.currentFragment.requireView().findViewById(R.id.overlay_icon_moving)
                                .setVisibility(parent.remoteParams.isMoving() ? View.VISIBLE : View.INVISIBLE);
                        if (parent.remoteParams.isMoving()) blinkImage(
                                parent.currentFragment.requireView().findViewById(R.id.overlay_icon_moving));
                        else
                            parent.currentFragment.requireView().findViewById(R.id.overlay_icon_moving).clearAnimation();
                        break;
                    case TYPE_NORMAL_TEXT:
                        parent.currentFragment.requireView().findViewById(R.id.overlay_text_moving)
                                .setVisibility(parent.remoteParams.isMoving() ? View.VISIBLE : View.INVISIBLE);
                        if (parent.remoteParams.isMoving()) blinkImage(
                                parent.currentFragment.requireView().findViewById(R.id.overlay_text_moving));
                        else
                            parent.currentFragment.requireView().findViewById(R.id.overlay_text_moving).clearAnimation();
                        break;
                    case TYPE_SETTINGS:
                    case TYPE_NONE:
                        break;
                }
            }
        });
    }

    // https://stackoverflow.com/a/27441001/8141824
    private void blinkImage(@NotNull ImageView image) {
        Animation animation = new AlphaAnimation((float) 0.5, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter
        // animation
        // rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation
        // infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
        // end so the button will
        // fade back in
        image.startAnimation(animation);
    }

    public void onCameraRotationChanged() {
        if (parent.currentFragment != null && parent.currentFragment.getView() != null)
        parent.runOnUiThread(() -> {
            if (parent.localParams.getCurrentOverlay() == Constants.OverlayType.TYPE_DIAGNOSTICS) {
                ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_camera_x))
                        .setText(parent.localParams.twoDP.format(parent.remoteParams.getCameraRotation()));
            }
        });
    }

    public void onRotationChanged() {
        if (parent.currentFragment.getView() != null)
        parent.runOnUiThread(() -> {
            if (parent.localParams.getCurrentOverlay() == Constants.OverlayType.TYPE_DIAGNOSTICS) {
                ((TextView) parent.currentFragment.requireView().findViewById(R.id.overlay_diag_robot_x))
                        .setText(parent.localParams.twoDP.format(parent.remoteParams.getRobotRotation()));
            }
        });
    }
}
