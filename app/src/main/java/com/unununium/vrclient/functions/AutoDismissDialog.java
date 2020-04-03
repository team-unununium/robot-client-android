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

package com.unununium.vrclient.functions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

/** An AlertDialog that would br dismissed when rotated.
 * The default button values for the arrays are [BUTTON_POSITIVE, BUTTON_NEGATIVE or BUTTON_NEUTRAL].
 * An IllegalArgumentException will be thrown in the constructor when any of the arrays provided
 * does not have a length of 3.
 * The AlertDialog will be dismissed when it is rotated based on its tag in MainActivity. **/
public class AutoDismissDialog extends DialogFragment {
    private static final int DIALOG_CONTENT_MESSAGE = 1;
    private static final int DIALOG_CONTENT_VIEW = 2;
    private static final int DIALOG_CONTENT_LIST = 3;

    private String title, message;
    private String[] buttonList;
    private int contentType, arrayRes;
    private boolean autoDismiss, cancellable = true;

    private View displayView;
    private DialogInterface.OnClickListener arrayListener;
    private DialogInterface.OnClickListener[] yListeners = new DialogInterface.OnClickListener[0];
    private DialogInterface.OnShowListener nListener;
    private DialogInterface.OnDismissListener dismissListener = null;

    /** Default constructor, used only when the app is rotated. **/
    public AutoDismissDialog() {
        autoDismiss = true;
        contentType = DIALOG_CONTENT_MESSAGE;
    }

    /** Constructor used for showing a view with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons. **/
    public AutoDismissDialog(String title, View displayView, @NotNull String[] buttonList) {
        DialogInterface.OnClickListener[] yListeners = new DialogInterface
                .OnClickListener[]{null, null, null};
        if (buttonList.length == 3) {
            this.title = title;
            this.displayView = displayView;
            this.buttonList = buttonList;
            this.yListeners = yListeners;
            this.contentType = DIALOG_CONTENT_VIEW;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3");
        }
    }

    /** Constructor used for showing a message with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param message is the message of the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, String message, String[] buttonList,
                             DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.message = message;
            this.buttonList = buttonList;
            this.yListeners = yListeners;
            this.contentType = DIALOG_CONTENT_MESSAGE;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3 or " +
                    "length of yListeners is not 3");
        }
    }

    /** Constructor used for showing a view with auto dismiss after a button press.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, @NotNull String[] buttonList,
                             DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.displayView = displayView;
            this.buttonList = buttonList;
            this.yListeners = yListeners;
            this.contentType = DIALOG_CONTENT_VIEW;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3 or " +
                    "length of yListeners is not 3");
        }
    }

    /** Constructor used for showing a view which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param arrayRes is the resource file pointing to a string array to be used in the AlertDialog
     * @param arrayListener is the OnClickListener for the elements in the array.
     * @param buttonList is the display values for all 3 buttons.
     * @param yListeners are the listeners that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, int arrayRes, DialogInterface.OnClickListener arrayListener,
                             @NotNull String[] buttonList, DialogInterface.OnClickListener[] yListeners) {
        if (buttonList.length == 3 && yListeners.length == 3) {
            this.title = title;
            this.arrayRes = arrayRes;
            this.buttonList = buttonList;
            this.arrayListener = arrayListener;
            this.contentType = DIALOG_CONTENT_LIST;
            this.autoDismiss = true;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3 or " +
                    "length of yListeners is not 3");
        }
    }

    /** Constructor used for showing a message which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param message is the message of the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param nListener is the OnShowListener that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, String message, @NotNull String[] buttonList,
                             DialogInterface.OnShowListener nListener) {
        if (buttonList.length == 3) {
            this.title = title;
            this.message = message;
            this.buttonList = buttonList;
            this.nListener = nListener;
            this.contentType = DIALOG_CONTENT_MESSAGE;
            this.autoDismiss = false;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3");
        }
    }

    /** Constructor used for showing a view which would only dismiss after the
     * correct button is pressed.
     * @param title is the title of the AlertDialog.
     * @param displayView is the view that would be displayed in the AlertDialog.
     * @param buttonList is the display values for all 3 buttons.
     * @param nListener is the OnShowListener that would be used in the AlertDialog. **/
    public AutoDismissDialog(String title, View displayView, @NotNull String[] buttonList,
                             DialogInterface.OnShowListener nListener) {
        if (buttonList.length == 3) {
            this.title = title;
            this.displayView = displayView;
            this.buttonList = buttonList;
            this.nListener = nListener;
            this.contentType = DIALOG_CONTENT_VIEW;
            this.autoDismiss = false;
        } else {
            throw new IllegalArgumentException("Length of buttonList is not 3");
        }
    }

    /** Sets the OnDismissListener for the AlertDialog. **/
    public void setDismissListener(DialogInterface.OnDismissListener listener) {
        dismissListener = listener;
    }

    /** Sets whether the AlertDialog can be cancelled **/
    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    /** Dismiss the dialog if the last one is still showing. **/
    @Override
    public void onStart() {
        if (getDialog() != null && getDialog().isShowing()) {
            getDialog().dismiss();
        }
        super.onStart();
    }

    /** Creates the actual AlertDialog based on the given parameters.
     * A NullPointerException is thrown if the context is null. **/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(title);
            if (contentType == DIALOG_CONTENT_VIEW) {
                builder.setView(displayView);
            } else if (contentType == DIALOG_CONTENT_MESSAGE) {
                builder.setMessage(message);
            } else if (contentType == DIALOG_CONTENT_LIST) {
                builder.setItems(arrayRes, arrayListener);
            }
            if (autoDismiss && buttonList != null && yListeners != null
                    && contentType != DIALOG_CONTENT_LIST) {
                builder.setPositiveButton(buttonList[0], yListeners[0]);
                builder.setNegativeButton(buttonList[1], yListeners[1]);
                builder.setNeutralButton(buttonList[2], yListeners[2]);
            } else if (buttonList != null) {
                builder.setPositiveButton(buttonList[0], null)
                        .setNegativeButton(buttonList[1], null)
                        .setNeutralButton(buttonList[2], null);
            }
            setCancelable(cancellable);

            // Sets the dialog listeners if autoDismiss is false
            AlertDialog dialog = builder.create();
            if (!autoDismiss) {
                dialog.setOnShowListener(nListener);
            }
            if (dismissListener != null) {
                dialog.setOnDismissListener(dismissListener);
            }
            return dialog;
        } else {
            throw new NullPointerException("Context for AutoDismissDialog is null");
        }
    }
}
