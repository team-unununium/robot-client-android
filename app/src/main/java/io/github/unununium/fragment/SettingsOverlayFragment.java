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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;
import io.github.unununium.util.FragmentOnBackPressed;

public class SettingsOverlayFragment extends PreferenceFragmentCompat implements FragmentOnBackPressed,
        PreferenceManager.OnPreferenceTreeClickListener {
    private final MainActivity parentActivity;
    private static final String VALID_FLOAT = "^[0-9]+(\\.[0-9]+)*$";

    public SettingsOverlayFragment(MainActivity parentActivity) {
        super();
        this.parentActivity = parentActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View targetView = super.onCreateView(inflater, container, savedInstanceState);
        if (targetView != null) {
            targetView.setOnClickListener((v) -> parentActivity.inputHandler.onToggleUpperOverlay());
            targetView.findViewById(R.id.so_cancel).setOnClickListener((v) -> parentActivity.inputHandler.onToggleUpperOverlay());
            targetView.findViewById(R.id.so_done).setOnClickListener((v) -> onDonePressed());
        }
        return targetView;
    }

    public void onDonePressed() {
        if (parentActivity.remoteParams.isOperator()) onOperatorDonePressed();
        if (switchPrefValueChanged("pref_toggle_ui_visibility", !parentActivity.localParams.uiIsHidden))
            parentActivity.localParams.uiIsHidden = !parentActivity.localParams.uiIsHidden;
        if (switchPrefValueChanged("pref_invert_ui_colour", !parentActivity.localParams.isDay))
            parentActivity.localParams.isDay = !parentActivity.localParams.isDay;
        if (switchPrefValueChanged("pref_display_icons", !parentActivity.localParams.normalOverlayIsText))
            parentActivity.localParams.normalOverlayIsText = !parentActivity.localParams.normalOverlayIsText;
        if (switchPrefValueChanged("pref_diagnostics", parentActivity.localParams.diagnosticsModeEnabled))
            parentActivity.localParams.diagnosticsModeEnabled = !parentActivity.localParams.diagnosticsModeEnabled;
        parentActivity.localParams.lowerTempBound =
                getFloatValue("pref_lower_temp_bound", parentActivity.localParams.lowerTempBound);
        parentActivity.localParams.upperTempBound =
                getFloatValue("pref_upper_temp_bound", parentActivity.localParams.upperTempBound);
        parentActivity.localParams.lowerHumidityBound =
                getFloatValue("pref_lower_humidity_bound", parentActivity.localParams.lowerHumidityBound);
        parentActivity.localParams.upperHumidityBound =
                getFloatValue("pref_upper_humidity_bound", parentActivity.localParams.upperHumidityBound);
        parentActivity.localParams.frontObstacleAmount =
                getFloatValue("pref_front_obstacle", parentActivity.localParams.frontObstacleAmount);
        parentActivity.localParams.rearObstacleAmount =
                getFloatValue("pref_rear_obstacle", parentActivity.localParams.rearObstacleAmount);
        parentActivity.localParams.coWarnLevel =
                getFloatValue("pref_co_level", parentActivity.localParams.coWarnLevel);
        parentActivity.localParams.ch4WarnLevel =
                getFloatValue("pref_ch4_level", parentActivity.localParams.ch4WarnLevel);
        parentActivity.localParams.h2WarnLevel =
                getFloatValue("pref_h2_level", parentActivity.localParams.h2WarnLevel);
        parentActivity.localParams.lpgWarnLevel =
                getFloatValue("pref_lpg_level", parentActivity.localParams.lpgWarnLevel);
        parentActivity.inputHandler.onToggleUpperOverlay();
    }

    public void onOperatorDonePressed() {
        // TODO: Complete
    }

    private boolean switchPrefValueChanged(String key, boolean compare) {
        SwitchPreference preference = findPreference(key);
        if (preference == null) throw new RuntimeException(key);
        return Objects.requireNonNull(preference).isChecked() != compare;
    }

    private float getFloatValue(String key, float fallback) {
        EditTextPreference preference = findPreference(key);
        String text = Objects.requireNonNull(preference).getText();
        if (text.matches(VALID_FLOAT)) return Float.parseFloat(text);
        else {
            Toast.makeText(parentActivity, "Invalid " + preference.getTitle().toString(), Toast.LENGTH_SHORT).show();
            return fallback;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (parentActivity.remoteParams.isOperator()) setPreferencesFromResource(R.xml.settings_operator, rootKey);
        else setPreferencesFromResource(R.xml.settings_observer, rootKey);
        PreferenceManager pManager = getPreferenceManager();
        pManager.setOnPreferenceTreeClickListener(SettingsOverlayFragment.this);
        customizePreferenceValues();
    }

    private void customizePreferenceValues() {
        if (parentActivity.remoteParams.isOperator()) customizeOperatorValues();
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_toggle_ui_visibility")))
                .setChecked(!parentActivity.localParams.uiIsHidden);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_invert_ui_colour")))
                .setChecked(!parentActivity.localParams.isDay);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_display_icons")))
                .setChecked(!parentActivity.localParams.normalOverlayIsText);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_diagnostics")))
                .setChecked(parentActivity.localParams.diagnosticsModeEnabled);

        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_lower_temp_bound")))
                .setText(parentActivity.localParams.oneDP.format(parentActivity.localParams.lowerTempBound));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_upper_temp_bound")))
                .setText(parentActivity.localParams.oneDP.format(parentActivity.localParams.upperTempBound));

        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_lower_humidity_bound")))
                .setText(parentActivity.localParams.twoDP.format(parentActivity.localParams.lowerHumidityBound));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_upper_humidity_bound")))
                .setText(parentActivity.localParams.twoDP.format(parentActivity.localParams.upperHumidityBound));

        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_front_obstacle")))
                .setText(parentActivity.localParams.oneDP.format(parentActivity.localParams.frontObstacleAmount));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_rear_obstacle")))
                .setText(parentActivity.localParams.oneDP.format(parentActivity.localParams.rearObstacleAmount));

        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_co_level")))
                .setText(parentActivity.localParams.fourDP.format(parentActivity.localParams.coWarnLevel));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_ch4_level")))
                .setText(parentActivity.localParams.fourDP.format(parentActivity.localParams.ch4WarnLevel));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_h2_level")))
                .setText(parentActivity.localParams.fourDP.format(parentActivity.localParams.h2WarnLevel));
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_lpg_level")))
                .setText(parentActivity.localParams.fourDP.format(parentActivity.localParams.lpgWarnLevel));
    }

    private void customizeOperatorValues() {
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_enable_external_controller")))
                .setChecked(parentActivity.localParams.externalControllerEnabled);
        ((ListPreference) Objects.requireNonNull(findPreference("pref_phone_controls")))
                .setValueIndex(parentActivity.localParams.getControlModeInt());
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_start_moving")))
                .setChecked(parentActivity.remoteParams.isMoving());
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_night_mode")))
                .setChecked(false);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_night_mode")))
                .setEnabled(false);
    }

    @Override
    public boolean onPreferenceTreeClick(@NotNull Preference preference) {
        if (Objects.equals(preference.getKey(), "pref_about")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/team-unununium/robot-client-android"));
            startActivity(intent);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onBackPressed() {
        parentActivity.inputHandler.onToggleUpperOverlay();
        return false;
    }
}