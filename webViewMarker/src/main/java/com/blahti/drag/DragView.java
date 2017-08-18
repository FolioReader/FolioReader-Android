/*
 * This is a modified version of a class from the Android Open Source Project. 
 * The original copyright and license information follows.
 * 
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blahti.drag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * A DragView is a special view used by a DragController. During a drag operation, what is actually moving
 * on the screen is a DragView. A DragView is constructed using a bitmap of the view the user really
 * wants to move.
 *
 */

public class DragView extends View {
    private static final boolean DEBUG = false;
    private static final int PADDING_TO_SCALE = 0;
    private final int mRegistrationX;
    private final int mRegistrationY;
    private Bitmap mBitmap;
    private Paint mDebugPaint = new Paint();
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    public DragView(Context context) throws Exception {
        super(context);
        mRegistrationX = 0;
        mRegistrationY = 0;
        throw new Exception("DragView constructor permits only programatical calling");
    }

    /**
     * Construct the drag view.
     * <p>
     * The registration point is the point inside our view that the touch events should
     * be centered upon.
     *
     * @param context A context
     * @param bitmap The view that we're dragging around.  We scale it up when we draw it.
     * @param registrationX The x coordinate of the registration point.
     * @param registrationY The y coordinate of the registration point.
     */
    public DragView(Context context, Bitmap bitmap, int registrationX, int registrationY, int left, int top, int width, int height) {
        super(context);
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);        
        mRegistrationX = registrationX + (PADDING_TO_SCALE / 2);
        mRegistrationY = registrationY + (PADDING_TO_SCALE / 2);
        final float scaleFactor = ((float)width + PADDING_TO_SCALE) / (float)width;
        final Matrix scale = new Matrix();
        scale.setScale(scaleFactor, scaleFactor);
        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height, scale, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (DEBUG) {
            mDebugPaint.setStyle(Paint.Style.FILL);
            mDebugPaint.setColor(0x88dd0011);
            canvas.drawRect(0, 0, getWidth(), getHeight(), mDebugPaint);
        }
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmap.recycle();
    }

    void show(IBinder windowToken, int touchX, int touchY) {
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            touchX - mRegistrationX, touchY - mRegistrationY,
            WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        lp.token = windowToken;
        lp.setTitle("DragView");
        mLayoutParams = lp;
        mWindowManager.addView(this, lp);
    }
    void move(int touchX, int touchY) {
        WindowManager.LayoutParams lp = mLayoutParams;
        lp.x = touchX - mRegistrationX;
        lp.y = touchY - mRegistrationY;
        mWindowManager.updateViewLayout(this, lp);
    }
    void remove() {
        mWindowManager.removeView(this);
    }
}
