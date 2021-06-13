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

public class NormalOverlayFragment extends OverlayFragment {
    private final boolean isText;

    public NormalOverlayFragment(MainActivity parentActivity, boolean isText, boolean initIsDay) {
        super(parentActivity, initIsDay);
        this.isText = isText;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Sets the text and image resources. **/
    @Override
    protected void setIntLists() {
        if (isText) {
            super.textViewList = new int[]{ R.id.overlay_temp_text, R.id.overlay_humidity_text,
                    R.id.overlay_front_obstacle_text, R.id.overlay_back_obstacle_text, R.id.overlay_gas_text };
            super.imageViewList = new int[]{ R.id.overlay_text_moving, R.id.overlay_text_disconnected, 
                    R.id.overlay_text_screenshot, R.id.overlay_text_settings };
            super.dayImageResList = new int[]{ R.drawable.ic_car_50_day, 
                    R.drawable.ic_cloud_off_50_day, R.drawable.ic_camera_50_day, R.drawable.ic_settings_50_day };
            super.nightImageResList = new int[]{ R.drawable.ic_car_50_night, 
                    R.drawable.ic_cloud_off_50_night, R.drawable.ic_camera_50_night, R.drawable.ic_settings_50_night };
            super.operatorOnlyList = new int[]{ R.id.overlay_text_moving, R.id.overlay_text_start_moving };
        } else {
            // The temperature and humidity icons' colour are not changed as there are multiple types of icons
            super.textViewList = new int[]{};
            super.imageViewList = new int[]{ R.id.overlay_icon_moving, R.id.overlay_icon_disconnected, 
                    R.id.overlay_icon_screenshot, R.id.overlay_icon_settings, R.id.overlay_obstacle_icon, 
                    R.id.overlay_obstacle_front_icon, R.id.overlay_obstacle_back_icon, 
                    R.id.overlay_co_icon, R.id.overlay_ch4_icon, R.id.overlay_h2_icon, 
                    R.id.overlay_lpg_icon };
            super.dayImageResList = new int[]{ R.drawable.ic_car_50_day, R.drawable.ic_cloud_off_50_day, 
                    R.drawable.ic_camera_50_day, R.drawable.ic_settings_50_day, R.drawable.ic_obstacle_day, 
                    R.drawable.ic_obstacle_front_day, R.drawable.ic_obstacle_back_day, 
                    R.drawable.ic_gas_co_day, R.drawable.ic_gas_ch4_day, 
                    R.drawable.ic_gas_h2_day, R.drawable.ic_gas_lpg_day };
            super.nightImageResList = new int[]{ R.drawable.ic_car_50_night, R.drawable.ic_cloud_off_50_night,
                    R.drawable.ic_camera_50_night, R.drawable.ic_settings_50_night, R.drawable.ic_obstacle_night,
                    R.drawable.ic_obstacle_front_night, R.drawable.ic_obstacle_back_night,
                    R.drawable.ic_gas_co_night, R.drawable.ic_gas_ch4_night,
                    R.drawable.ic_gas_h2_night, R.drawable.ic_gas_lpg_night };
            super.operatorOnlyList = new int[]{ R.id.overlay_icon_moving, R.id.overlay_icon_start_moving };
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int res = isText ? R.layout.fragment_normal_text_overlay : R.layout.fragment_normal_icon_overlay;
        View returnView = inflater.inflate(res, container, false);
        setViewListeners(returnView);
        setOperatorViewsVisibility(returnView, parentActivity.params.isOperator());
        return returnView;
    }

    /** Set the onClickListeners for the view. **/
    @Override
    protected void setViewListeners(@NotNull View view) {
        if (isText) {
            view.findViewById(R.id.overlay_text_screenshot)
                    .setOnClickListener(v -> parentActivity.inputHandler.onScreenshot());
            view.findViewById(R.id.overlay_text_settings)
                    .setOnClickListener(v -> parentActivity.inputHandler.onToggleUpperOverlay());
        } else {
            view.findViewById(R.id.overlay_icon_screenshot)
                    .setOnClickListener(v -> parentActivity.inputHandler.onScreenshot());
            view.findViewById(R.id.overlay_icon_settings)
                    .setOnClickListener(v -> parentActivity.inputHandler.onToggleUpperOverlay());
        }
    }

    /** Manually swap the colours for the temperature and humidity icons. **/
    @Override
    public void swapColour(boolean isDay) {
        super.swapColour(isDay);
        // TODO: Swap colours for temp and humidity
    }
}