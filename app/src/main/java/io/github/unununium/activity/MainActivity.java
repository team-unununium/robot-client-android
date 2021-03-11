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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.modules.core.PermissionListener;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import io.github.unununium.BuildConfig;
import io.github.unununium.R;
import io.github.unununium.fragment.AboutOverlayFragment;
import io.github.unununium.fragment.DiagnosticsOverlayFragment;
import io.github.unununium.fragment.NormalOverlayFragment;
import io.github.unununium.fragment.SettingsOverlayFragment;
import io.github.unununium.util.Constants;
import io.github.unununium.util.FragmentOnBackPressed;
import io.github.unununium.util.GeneralFunctions;
import io.github.unununium.util.InputHandler;

/** The main Activity for the app, handles displaying the videos only
 * as the UI is handled by the overlay fragments. **/
public class MainActivity extends AppCompatActivity implements JitsiMeetActivityInterface {
    public Fragment currentFragment = null;
    public InputHandler inputHandler = null;
    private JitsiMeetView videoCallView = null;

    public boolean normalOverlayIsText = false;
    public boolean diagnosticsModeEnabled = false;
    public boolean upperOverlayIsSettings = true;
    public boolean uiIsHidden = false;
    public boolean upperOverlayIsHidden = true;

    private int lastControllerID = 0;
    private final int uid = new Random().nextInt();
    private boolean doubleBackToExitPressedOnce = false;

    /** A broadcast receiver that forwards the Jitsi Meet intent received to the activity. **/
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };

    /** Creates the view and sets up the options for the view. **/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputHandler = new InputHandler(MainActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastEvent.Type.PARTICIPANT_JOINED.getAction());
        intentFilter.addAction(BroadcastEvent.Type.PARTICIPANT_LEFT.getAction());
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
        showOverlay(Constants.OverlayType.TYPE_NORMAL_TEXT);
    }

    /** Pauses the live stream. **/
    @Override
    protected void onPause() {
        super.onPause();
        stopCall();
    }

    /** Resumes the live stream. **/
    @Override
    protected void onResume() {
        super.onResume();
        setImmersiveSticky();
        joinCall();
    }

    /** Resumes the stream on the player. **/
    private void joinCall() {
        JitsiMeetConferenceOptions conferenceOptions = initConferenceOptions();
        if (videoCallView == null) initVideoCallView();
        videoCallView.join(conferenceOptions);
    }

    /** Initializes the video call view and adds it to the FrameLayout, deleting any previous
     * video call views that may exist. **/
    private void initVideoCallView() {
        FrameLayout videoCallRoot = findViewById(R.id.m1_playerview);
        videoCallRoot.removeAllViews();
        videoCallView = new JitsiMeetView(MainActivity.this);
        videoCallRoot.addView(videoCallView);
    }

    /** Stops the video call. **/
    private void stopCall() {
        if (videoCallView != null) videoCallView.leave();
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
                stopCall();
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
        // TODO: Complete
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
                // TODO: Start / stop moving
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
                // TODO: Decrease speed
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_UP_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP_RIGHT:
                // Allow some leeway for error
                // TODO: Increase speed
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
        lastControllerID = event.getDeviceId();
        return true;
    }

    /** Show a specific overlay.
     * AlertDialogs could not be used as they call onPause on the Activity. **/
    public void showOverlay(@NotNull Constants.OverlayType overlayType) {
        Fragment targetFragment;
        FragmentManager fm = getSupportFragmentManager();
        switch (overlayType) {
            case TYPE_ABOUT:
                targetFragment = new AboutOverlayFragment(MainActivity.this);
                break;
            case TYPE_SETTINGS:
                targetFragment = new SettingsOverlayFragment(MainActivity.this);
                break;
            case TYPE_DIAGNOSTICS:
                targetFragment = new DiagnosticsOverlayFragment(MainActivity.this, inputHandler.isDay);
                break;
            case TYPE_NORMAL_TEXT:
                targetFragment = new NormalOverlayFragment(MainActivity.this, true, inputHandler.isDay);
                break;
            case TYPE_NORMAL_ICON:
                targetFragment = new NormalOverlayFragment(MainActivity.this, false, inputHandler.isDay);
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
            uiIsHidden = true;
        } else {
            if (currentFragment instanceof SettingsOverlayFragment) {
                // TODO: On cancel pressed
            }
            // No special case for Settings page as the settings are cancelled anyways
            currentFragment = targetFragment;
            switch (overlayType) {
                case TYPE_ABOUT:
                    upperOverlayIsSettings = false;
                    upperOverlayIsHidden = false;
                    break;
                case TYPE_SETTINGS:
                    upperOverlayIsSettings = true;
                    upperOverlayIsHidden = false;
                    break;
                case TYPE_DIAGNOSTICS:
                    diagnosticsModeEnabled = true;
                    upperOverlayIsHidden = true;
                    break;
                case TYPE_NORMAL_ICON:
                    normalOverlayIsText = false;
                    diagnosticsModeEnabled = false;
                    upperOverlayIsHidden = true;
                    break;
                case TYPE_NORMAL_TEXT:
                    normalOverlayIsText = true;
                    diagnosticsModeEnabled = false;
                    upperOverlayIsHidden = true;
                    break;
                case TYPE_NONE:
                    // It should be handled in the above statement already
                    break;
            }
            uiIsHidden = false;
            ft.replace(R.id.m1_overlay, currentFragment, "MainActivity.Overlay");
        }
        ft.commit();
    }

    /** Initializes the conference options for the meeting. **/
    private JitsiMeetConferenceOptions initConferenceOptions() {
        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(String.format(Locale.ENGLISH, "UC v%s %d",
                BuildConfig.VERSION_NAME, uid));
        return new JitsiMeetConferenceOptions.Builder()
                .setUserInfo(userInfo)
                .setRoom(BuildConfig.JITSI_ROOM_URL)
                .setVideoMuted(true)
                .setAudioMuted(true)
                .setAudioOnly(false)
                .setWelcomePageEnabled(false)
                .build();
    }

    /** The broadcast receiver for the Jitsi Meet view. **/
    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);
            switch (event.getType()) {
                case CONFERENCE_JOINED:
                case CONFERENCE_WILL_JOIN:
                case CONFERENCE_TERMINATED:
                case PARTICIPANT_LEFT:
                    break;
                case PARTICIPANT_JOINED:
                    onParticipantJoined(event.getData());
                    break;
            }
        }
    }

    /** Checks for whether the target user just entered if it is not already being focused on. **/
    private void onParticipantJoined(@NotNull HashMap<String, Object> data) {
        String displayName = (String) data.get("name");
        String id = (String) data.get("participantId");
        if (Objects.equals(displayName, BuildConfig.JITSI_ROBOT_USER)) {
            Intent pinParticipantIntent = BroadcastIntentHelper.buildSelectUserVideoIntent(id);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(pinParticipantIntent);
        }
    }

    /** Stub function. **/
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    /** Request the permissions needed. **/
    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {
        super.requestPermissions(strings, i);
    }
}