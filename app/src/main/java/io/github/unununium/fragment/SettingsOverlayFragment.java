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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.github.unununium.R;
import io.github.unununium.activity.MainActivity;
import io.github.unununium.util.FragmentOnBackPressed;

public class SettingsOverlayFragment extends PreferenceFragmentCompat implements FragmentOnBackPressed,
        PreferenceManager.OnPreferenceTreeClickListener {
    private final MainActivity parentActivity;

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
        // TODO: Complete
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        PreferenceManager pManager = getPreferenceManager();
        pManager.setOnPreferenceTreeClickListener(SettingsOverlayFragment.this);
        customizePreferenceValues();
    }

    private void customizePreferenceValues() {
        // TODO: Complete
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