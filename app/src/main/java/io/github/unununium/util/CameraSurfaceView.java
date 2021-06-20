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

import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

import io.github.unununium.R;

/** A custom SurfaceView to draw footage from the latest frame. **/
public class CameraSurfaceView extends SurfaceView {
    private MediaCodec h264Decoder;

    private static final int FPS = 30;
    private static final String VIDEO_MIME = "video/h264";

    private boolean threadRunning = false;
    private final Thread updateThread = new Thread(){
        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            threadRunning = true;
            while (threadRunning) {
                // TODO: Update data
                try {
                    sleep(1000 / FPS);
                } catch (InterruptedException ignored) {

                }
            }
        }
    };

    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        try {
            h264Decoder = MediaCodec.createDecoderByType(VIDEO_MIME);
            updateThread.start();
        } catch (IOException e) {
            Toast.makeText(getContext(), R.string.error_h264_not_supported, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void terminate() {
        threadRunning = false;
        updateThread.interrupt();
    }

    public void setVideo(byte[] video) {
        // TODO: Set media format
        h264Decoder.configure(null, getHolder().getSurface(), null, 0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }
}
