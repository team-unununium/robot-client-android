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
            // TODO: Handle data receiving
            switch (parent.localParams.getCurrentOverlay()) {
                case TYPE_DIAGNOSTICS:
                    break;
                case TYPE_NORMAL_ICON:
                    break;
                case TYPE_NORMAL_TEXT:
                    break;
                case TYPE_SETTINGS:
                case TYPE_NONE:
                    break;
            }
        });
    }

    public void onStreamInfoReceived() {
        parent.runOnUiThread(() -> {
            CameraSurfaceView surfaceView = parent.findViewById(R.id.m1_playerview);
            surfaceView.bufferDuration = parent.remoteParams.getBufferDuration();
            surfaceView.videoWidth = parent.remoteParams.getVideoWidth();
            surfaceView.videoHeight = parent.remoteParams.getVideoHeight();
            // TODO: Handle stream info receiving
            switch (parent.localParams.getCurrentOverlay()) {
                case TYPE_DIAGNOSTICS:
                    break;
                case TYPE_NORMAL_ICON:
                    break;
                case TYPE_NORMAL_TEXT:
                    break;
                case TYPE_SETTINGS:
                case TYPE_NONE:
                    break;
            }
        });
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
