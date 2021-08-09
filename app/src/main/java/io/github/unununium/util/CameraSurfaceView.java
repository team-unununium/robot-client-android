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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.UnsupportedFormatException;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.Picture;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.github.unununium.R;

import static android.graphics.Bitmap.Config.ARGB_8888;

/** A custom SurfaceView to draw footage from the latest frame. **/
public class CameraSurfaceView extends SurfaceView {

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
    }

    public void setCurrentImage(byte[] buffer) {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            Bitmap currentBitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
            float ar = canvas.getHeight() * 1.0f / currentBitmap.getScaledHeight(canvas);
            Bitmap rescaledBitmap = Bitmap.createScaledBitmap(currentBitmap,
                    (int) (ar * currentBitmap.getScaledWidth(canvas)), getHeight(), false);
            float left = (canvas.getWidth() - rescaledBitmap.getScaledWidth(canvas)) / 2f;
            canvas.drawBitmap(rescaledBitmap, left, 0, new Paint());
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }
}
