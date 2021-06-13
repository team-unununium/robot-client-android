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

package io.github.unununium.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;

public class DiagnosticsOverlayFragment extends OverlayFragment {
    public DiagnosticsOverlayFragment(MainActivity parentActivity, boolean initIsDay) {
        super(parentActivity, initIsDay);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Sets the text and image resources. **/
    @Override
    protected void setIntLists() {
        super.textViewList = new int[]{ R.id.overlay_diag_temp, R.id.overlay_diag_humidity, 
                R.id.overlay_diag_front_obstacle, R.id.overlay_diag_back_obstacle, 
                R.id.overlay_diag_co_level, R.id.overlay_diag_ch4_level, 
                R.id.overlay_diag_h2_level, R.id.overlay_diag_lpg_level, 
                R.id.overlay_diag_server, R.id.overlay_diag_app_mode, R.id.overlay_diag_night_mode, 
                R.id.overlay_diag_external_controller, R.id.overlay_diag_phone_mode,
                R.id.overlay_diag_last_camera_rotation, R.id.overlay_diag_camera_x, 
                R.id.overlay_diag_camera_y, R.id.overlay_diag_camera_z,
                R.id.overlay_diag_last_robot_rotation, R.id.overlay_diag_robot_x,
                R.id.overlay_diag_robot_y, R.id.overlay_diag_robot_z };
        super.imageViewList = new int[]{ R.id.overlay_diag_screenshot, R.id.overlay_diag_settings };
        super.dayImageResList = new int[]{ R.drawable.ic_camera_50_day, R.drawable.ic_settings_50_day };
        super.nightImageResList = new int[]{ R.drawable.ic_camera_50_night, R.drawable.ic_settings_50_night };
        super.operatorOnlyList = new int[]{ R.id.overlay_diag_start_moving, R.id.overlay_diag_night_mode,
            R.id.overlay_diag_external_controller, R.id.overlay_diag_phone_mode, R.id.overlay_diag_last_camera_rotation,
            R.id.overlay_diag_camera_x, R.id.overlay_diag_camera_y, R.id.overlay_diag_camera_z,
            R.id.overlay_diag_last_robot_rotation, R.id.overlay_diag_robot_x, R.id.overlay_diag_robot_y,
            R.id.overlay_diag_robot_z };
    }

    /** Set the onClickListeners for the view. **/
    @Override
    protected void setViewListeners(@NotNull View view) {
        view.findViewById(R.id.overlay_diag_screenshot)
                .setOnClickListener(v -> parentActivity.inputHandler.onScreenshot());
        view.findViewById(R.id.overlay_diag_settings)
                .setOnClickListener(v -> parentActivity.inputHandler.onToggleUpperOverlay());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_diagnostics_overlay, container, false);
        setViewListeners(returnView);
        setOperatorViewsVisibility(returnView, parentActivity.params.isOperator());
        return returnView;
    }
}