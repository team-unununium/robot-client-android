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

package io.github.unununium.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

import io.github.unununium.R;
import io.github.unununium.comm.ConnectionParameters;
import io.github.unununium.comm.LocalParameters;
import io.github.unununium.comm.ServerConnection;
import io.github.unununium.fragment.DiagnosticsOverlayFragment;
import io.github.unununium.fragment.NormalOverlayFragment;
import io.github.unununium.fragment.SettingsOverlayFragment;
import io.github.unununium.util.CameraSurfaceView;
import io.github.unununium.util.Constants;
import io.github.unununium.util.FragmentOnBackPressed;
import io.github.unununium.util.GeneralFunctions;
import io.github.unununium.util.InputHandler;
import io.github.unununium.util.ValueHandler;

/** The main Activity for the app, handles displaying the videos only
 * as the UI is handled by the overlay fragments. **/
public class MainActivity extends AppCompatActivity {
    public Fragment currentFragment = null;
    public InputHandler inputHandler = null;
    public ValueHandler valueHandler = null;
    public ConnectionParameters remoteParams = new ConnectionParameters();
    public LocalParameters localParams = new LocalParameters();
    public ServerConnection serverConnection = null;
    private boolean doubleBackToExitPressedOnce = false;

    /** Creates the view and sets up the options for the view. **/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputHandler = new InputHandler(MainActivity.this);
        valueHandler = new ValueHandler(MainActivity.this);
        showOverlay(Constants.OverlayType.TYPE_NORMAL_TEXT);
    }

    /** Pauses the live stream. **/
    @Override
    protected void onPause() {
        super.onPause();
        if (serverConnection != null) serverConnection.pauseConnection();
    }

    /** Resumes the live stream. **/
    @Override
    protected void onResume() {
        super.onResume();
        if (serverConnection == null) {
            serverConnection = new ServerConnection(MainActivity.this);
            serverConnection.createConnection();
        } else {
            serverConnection.resumeConnection();
        }
        setImmersiveSticky();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverConnection.terminateConnection();
    }

    /** Set the top bar of the screen to be hidden. **/
    private void setImmersiveSticky() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /** Exits the app if the back button is pressed twice. **/
    @Override
    public void onBackPressed() {
        // Handle FragmentOnBackPressed
        if (currentFragment == null || !(currentFragment instanceof FragmentOnBackPressed) ||
                ((FragmentOnBackPressed) currentFragment).onBackPressed()) {
            // Press back to exit
            if (doubleBackToExitPressedOnce) {
                GeneralFunctions.exitApp(MainActivity.this);
            } else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, R.string.activity_back_exit, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1500);
            }
        }
    }

    /** Delegates the movements of the controller to the input handler. **/
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event);
        // TODO: Handle controller motion
    }

    /** Delegates the inputs by the controller to the input handler. **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        } else if (!GeneralFunctions.deviceIsController(event.getDevice())) {
            return false;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                // Prevent L1 and R1 from being triggered by joystick button presses
                break;
            case KeyEvent.KEYCODE_BUTTON_X:
                if (remoteParams.isOperator()) serverConnection.setMoving(!remoteParams.isMoving());
                break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                inputHandler.onInvertColour();
                break;
            case KeyEvent.KEYCODE_BUTTON_A:
                inputHandler.onToggleUI();
                break;
            case KeyEvent.KEYCODE_BUTTON_B:
                // TODO: Toggle camera night mode
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_DOWN_LEFT:
            case KeyEvent.KEYCODE_DPAD_DOWN_RIGHT:
                // Allow some leeway for error
                if (remoteParams.isOperator() && remoteParams.getVelocity() > 1) {
                    serverConnection.setVelocity(remoteParams.getVelocity() - 1);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_UP_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                // Allow some leeway for error
                if (remoteParams.isOperator() && remoteParams.getVelocity() < 3) {
                    serverConnection.setVelocity(remoteParams.getVelocity() + 1);
                }
                break;
            case KeyEvent.KEYCODE_BUTTON_L1:
                inputHandler.onScreenshot();
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                inputHandler.onToggleDiagnosticsMode();
                break;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
            case KeyEvent.KEYCODE_MENU:
                inputHandler.onToggleUpperOverlay();
                break;
            default:
                return false;
        }
        return true;
    }

    /** Show a specific overlay.
     * AlertDialogs could not be used as they call onPause on the Activity. **/
    public void showOverlay(@NotNull Constants.OverlayType overlayType) {
        Fragment targetFragment;
        FragmentManager fm = getSupportFragmentManager();
        switch (overlayType) {
            case TYPE_SETTINGS:
                targetFragment = new SettingsOverlayFragment(MainActivity.this);
                break;
            case TYPE_DIAGNOSTICS:
                targetFragment = new DiagnosticsOverlayFragment(MainActivity.this, localParams.isDay);
                break;
            case TYPE_NORMAL_TEXT:
                targetFragment = new NormalOverlayFragment(MainActivity.this, true, localParams.isDay);
                break;
            case TYPE_NORMAL_ICON:
                targetFragment = new NormalOverlayFragment(MainActivity.this, false, localParams.isDay);
                break;
            default:
                // Used to hide the UI
                targetFragment = null;
                break;
        }

        FragmentTransaction ft = fm.beginTransaction();
        if (targetFragment == null) {
            if (currentFragment != null) ft.remove(currentFragment);
            currentFragment = null;
            localParams.uiIsHidden = true;
        } else {
            // No special case for Settings page as the settings are cancelled anyways
            currentFragment = targetFragment;
            switch (overlayType) {
                case TYPE_SETTINGS:
                    localParams.upperOverlayIsHidden = false;
                    break;
                case TYPE_DIAGNOSTICS:
                    localParams.diagnosticsModeEnabled = true;
                    localParams.upperOverlayIsHidden = true;
                    break;
                case TYPE_NORMAL_ICON:
                    localParams.normalOverlayIsText = false;
                    localParams.diagnosticsModeEnabled = false;
                    localParams.upperOverlayIsHidden = true;
                    break;
                case TYPE_NORMAL_TEXT:
                    localParams.normalOverlayIsText = true;
                    localParams.diagnosticsModeEnabled = false;
                    localParams.upperOverlayIsHidden = true;
                    break;
                case TYPE_NONE:
                    // It should be handled in the above statement already
                    break;
            }
            localParams.uiIsHidden = false;
            ft.replace(R.id.m1_overlay, currentFragment, "MainActivity.Overlay");
        }
        ft.commit();
        setImmersiveSticky();
    }

    /** Stub function. **/
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}