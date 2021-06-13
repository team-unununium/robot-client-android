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

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.unununium.activity.MainActivity;

/** An overlay Fragment that receives variables from t  he activity. **/
public abstract class OverlayFragment extends Fragment {
    protected MainActivity parentActivity;
    private boolean initColourSet = false;
    private final boolean initIsDay;
    protected int[] textViewList = new int[]{};
    protected int[] imageViewList = new int[]{};
    protected int[] dayImageResList = new int[]{};
    protected int[] nightImageResList = new int[]{};
    protected int[] operatorOnlyList = new int[]{};

    public OverlayFragment(MainActivity parentActivity, boolean initIsDay) {
        this.parentActivity = parentActivity;
        this.initIsDay = initIsDay;
        // swapColour is not called immediately as the child would need to put in the values
        // for the int lists in its constructor first
    }

    /** Calls setIntLists to allow the values of the integer lists to be set. **/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setIntLists();
    }

    /** Set the values for the integer lists. **/
    protected void setIntLists() {}

    /** Sets the listeners for the view. **/
    protected void setViewListeners(View view) {}

    /** Sets whether operator specific views are visible. **/
    public void setOperatorViewsVisibility(View v, boolean visible) {
        View targetView = v == null ? getView() : v;
        if (targetView != null) {
            for (int i : operatorOnlyList) {
                if (targetView.findViewById(i) != null)
                    targetView.findViewById(i).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!initColourSet) {
            swapColour(initIsDay);
            initColourSet = true;
        }
    }

    /** Swap the colours of the text resources.
     * The local isDay is not trusted as the input handler handles the real tracking for isDay. **/
    public void swapColour(boolean isDay) {
        View rootView = getView();
        if (rootView == null) return; // Reduces the encapsulation count for ease of reading

        // Swaps all the text resources
        swapTextColour(isDay, rootView);
        swapImageColour(isDay, rootView);
    }

    /** Swaps all the text resources between white and black. **/
    private void swapTextColour(boolean isDay, View rootView) {
        for (int textRes: textViewList) {
            try {
                TextView textView = rootView.findViewById(textRes);
                if (textView != null) {
                    if (isDay) textView.setTextColor(Color.WHITE);
                    else textView.setTextColor(Color.BLACK);
                }
            } catch (ClassCastException e) {
                Log.w("OverlayFragment", "Attempting to cast a view that is not TextView to TextView");
                e.printStackTrace();
            }
        }
    }

    /** Swaps all the image resources between white and black. **/
    private void swapImageColour(boolean isDay, View rootView) {
        for (int i = 0; i < imageViewList.length; i++) {
            try {
                ImageView imageView = rootView.findViewById(imageViewList[i]);
                if (imageView != null) {
                    if (isDay) imageView.setImageResource(dayImageResList[i]);
                    else imageView.setImageResource(nightImageResList[i]);
                }
            } catch (ClassCastException e) {
                Log.w("OverlayFragment", "Attempting to cast a view that is not ImageView to ImageView");
                e.printStackTrace();
            }
        }
    }
}
