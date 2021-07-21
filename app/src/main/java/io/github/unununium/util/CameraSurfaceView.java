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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.Toast;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.github.unununium.R;

import static android.graphics.Bitmap.Config.ARGB_8888;

/** A custom SurfaceView to draw footage from the latest frame. **/
public class CameraSurfaceView extends SurfaceView {
    public double bufferDuration = 0.4;
    public int videoWidth = 1280;
    public int videoHeight = 720;
    public ArrayList<Bitmap> currentFrames = new ArrayList<>();
    private static final int FPS = 60;

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
        updateThread.start();
    }

    public void terminate() {
        threadRunning = false;
        updateThread.interrupt();
    }

    public void setVideo(byte[] video) {
        // TODO: Set media format
        ByteBuffer videoBuffer = ByteBuffer.wrap(video);
        ByteBufferSeekableByteChannel channel = null;
        try {
            channel = new ByteBufferSeekableByteChannel(videoBuffer, video.length);
            FrameGrab fg = FrameGrab.createFrameGrab(channel);
            fg.getNativeFrame();
        } catch (IOException | JCodecException e) {
            Toast.makeText(getContext(), R.string.error_buffer_parse, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    Toast.makeText(getContext(), R.string.error_buffer_parse, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    public static Bitmap toBitmap(@NotNull Picture src) {
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), ARGB_8888);
        toBitmap(src, dst);
        return dst;
    }

    public static void toBitmap(@NotNull Picture src, Bitmap dst) {
        byte[] srcData = src.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        for (int i = 0, dstOff = 0, srcOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, dstOff++, srcOff += 3) {
                packed[dstOff] = (srcData[srcOff] << 16) | (srcData[srcOff + 1] << 8) | srcData[srcOff + 2];
            }
        }
        dst.setPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
    }
}
