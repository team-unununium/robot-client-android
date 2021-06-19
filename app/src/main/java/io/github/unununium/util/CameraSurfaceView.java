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
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

/** A custom SurfaceView to draw footage from the latest frame. **/
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public Bitmap targetBitmap;
    private Paint paint;
    private boolean threadRunning = false;
    private static final int FPS = 30;
    private final Thread updateThread = new Thread(){
        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            threadRunning = true;
            while (threadRunning) {
                Canvas canvas = null;
                SurfaceHolder holder = getHolder();
                try {
                    canvas = holder.lockCanvas();
                    synchronized (getHolder()) {
                        draw(canvas);
                    }
                } catch (Exception ignored) {

                } finally {
                    if (canvas != null) {
                        try {
                            holder.unlockCanvasAndPost(canvas);
                        } catch (Exception ignored) {

                        }
                    }
                }
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

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.BLACK);
        if (paint == null) paint = new Paint();
        if (targetBitmap != null) {
            float scaling = getHeight() / (float) targetBitmap.getHeight();
            canvas.drawBitmap(Bitmap.createScaledBitmap(targetBitmap,
                    (int) Math.floor(targetBitmap.getWidth() * scaling), getHeight(), false),
                    (getWidth() - (float) targetBitmap.getWidth() * scaling) / 2, 0, paint);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}
