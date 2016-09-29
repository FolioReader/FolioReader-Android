/*
* Copyright (C) 2016 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.folioreader.view;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.Tags;
import com.folioreader.util.ViewHelper;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConfigView extends FrameLayout implements View.OnClickListener {

    private static final float SENSITIVITY = 1.0f;
    private static final float DEFAULT_DRAG_LIMIT = 0.5f;
    private static final int INVALID_POINTER = -1;
    private static final int FADE_DAY_NIGHT_MODE = 500;
    private static final int FONT_ANDADA = 1;
    private static final int FONT_LATO = 2;
    private static final int FONT_LORA = 3;
    private static final int FONT_RALEWAY = 4;

    private int mActivePointerId = INVALID_POINTER;

    private boolean mIsNightMode = false;

    private float mVerticalDragRange;

    private RelativeLayout mContainer;
    private ImageButton mDayButton;
    private ImageButton mNightButton;
    private SeekBar mFontSizeSeekBar;
    private ViewDragHelper mViewDragHelper;
    private ConfigViewCallback mConfigViewCallback;

    public ConfigView(Context context) {
        this(context, null);
    }

    public ConfigView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void inflateView() {
        inflate(getContext(), R.layout.view_config, this);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mFontSizeSeekBar = (SeekBar) findViewById(R.id.seekbar_font_size);
        mDayButton = (ImageButton) findViewById(R.id.day_button);
        mNightButton = (ImageButton) findViewById(R.id.night_button);
        mDayButton.setTag(Tags.DAY_BUTTON);
        mNightButton.setTag(Tags.NIGHT_BUTTON);
        mDayButton.setOnClickListener(this);
        mNightButton.setOnClickListener(this);
    }

    private void configFonts() {
        findViewById(R.id.btn_font_andada).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_ANDADA);
            }
        });
        findViewById(R.id.btn_font_lato).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_LATO);
            }
        });
        findViewById(R.id.btn_font_lora).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_LORA);
            }
        });
        findViewById(R.id.btn_font_raleway).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_RALEWAY);
            }
        });
    }

    private void selectFont(int selectedFont) {
        if (selectedFont == FONT_ANDADA) {
            findViewById(R.id.btn_font_andada).setSelected(true);
            findViewById(R.id.btn_font_lato).setSelected(false);
            findViewById(R.id.btn_font_lora).setSelected(false);
            findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_LATO) {
            findViewById(R.id.btn_font_andada).setSelected(false);
            findViewById(R.id.btn_font_lato).setSelected(true);
            findViewById(R.id.btn_font_lora).setSelected(false);
            findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_LORA) {
            findViewById(R.id.btn_font_andada).setSelected(false);
            findViewById(R.id.btn_font_lato).setSelected(false);
            findViewById(R.id.btn_font_lora).setSelected(true);
            findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_RALEWAY) {
            findViewById(R.id.btn_font_andada).setSelected(false);
            findViewById(R.id.btn_font_lato).setSelected(false);
            findViewById(R.id.btn_font_lora).setSelected(false);
            findViewById(R.id.btn_font_raleway).setSelected(true);
        }

        Config.getConfig().setFont(selectedFont - 1);
        if (mConfigViewCallback != null) mConfigViewCallback.onConfigChange();
    }

    private void toggleBlackTheme() {

        int day = getResources().getColor(R.color.white);
        int night = getResources().getColor(R.color.night);
        int darkNight = getResources().getColor(R.color.dark_night);
        final int diffNightDark = night - darkNight;

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                mIsNightMode ? night : day, mIsNightMode ? day : night);
        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int value = (int) animator.getAnimatedValue();
                mContainer.setBackgroundColor(value);
                if (mConfigViewCallback != null) {
                    mConfigViewCallback.onBackgroundUpdate(value - diffNightDark);
                }
            }
        });
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mIsNightMode = !mIsNightMode;
                Config.getConfig().setNightMode(mIsNightMode);
                mConfigViewCallback.onConfigChange();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
        colorAnimation.start();
    }

    private void configSeekbar() {
        mFontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Config.getConfig().setFontSize(progress);
                if (mConfigViewCallback != null) mConfigViewCallback.onConfigChange();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mFontSizeSeekBar.setProgress(Config.getConfig().getFontSize());
    }

    /**
     * Bind the attributes of the view and config
     * the DragView with these params.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            inflateView();
            configFonts();
            configSeekbar();
            configDragViewHelper();
            selectFont(Config.getConfig().getFont());
            mIsNightMode = Config.getConfig().isNightMode();
            if (mIsNightMode) {
                mDayButton.setSelected(false);
                mNightButton.setSelected(true);
            } else {
                mDayButton.setSelected(true);
                mNightButton.setSelected(false);
            }
        }
    }

    /**
     * Updates the view size if needed.
     *
     * @param width     The new width size.
     * @param height    The new height size.
     * @param oldWidth  The old width size, useful the calculate the diff.
     * @param oldHeight The old height size, useful the calculate the diff.
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        setVerticalDragRange(height);
    }

    /**
     * Configure the width and height of the DraggerView.
     *
     * @param widthMeasureSpec  Spec value of width, not represent the real width.
     * @param heightMeasureSpec Spec value of height, not represent the real height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY);
        int measureHeight = MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        if (mContainer != null) {
            mContainer.measure(measureWidth, measureHeight);
        }

    }

    /**
     * Detect the type of motion event (like touch)
     * at the DragView, this can be a simple
     * detector of the touch, not the listener ifself.
     *
     * @param ev Event of MotionEvent
     * @return View is touched
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mViewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
            default:
                return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }
    }

    /**
     * Handle the touch event intercepted from onInterceptTouchEvent
     * method, this method valid if the touch listener
     * is a valid pointer(like fingers) or the touch
     * is inside of the DragView.
     *
     * @param ev MotionEvent instance, can be used to detect the type of touch.
     * @return Touched area is a valid position.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int actionMasked = MotionEventCompat.getActionMasked(ev);
        if ((actionMasked & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            mActivePointerId = MotionEventCompat.getPointerId(ev, actionMasked);
        }
        if (mActivePointerId == INVALID_POINTER) {
            return false;
        }
        mViewDragHelper.processTouchEvent(ev);
        return ViewHelper.isViewHit(mContainer, this, (int) ev.getX(), (int) ev.getY());
    }

    @Override
    public void onClick(View v) {
        switch (((Integer) v.getTag())) {
            case Tags.DAY_BUTTON:
                if (mIsNightMode) {
                    mIsNightMode = true;
                    toggleBlackTheme();
                    mDayButton.setSelected(true);
                    mNightButton.setSelected(false);
                    ((Activity) getContext()).
                            findViewById(R.id.toolbar).
                            setBackgroundColor(getContext().getResources().getColor(R.color.white));
                    ((TextView) ((Activity) getContext()).
                            findViewById(R.id.lbl_center)).
                            setTextColor(getResources().getColor(R.color.black));
                    mConfigViewCallback.changeMenuTextColor();
                }
                break;
            case Tags.NIGHT_BUTTON:
                if (!mIsNightMode) {
                    mIsNightMode = false;
                    toggleBlackTheme();
                    mDayButton.setSelected(false);
                    mNightButton.setSelected(true);
                    ((Activity) getContext())
                            .findViewById(R.id.toolbar)
                            .setBackgroundColor(getContext()
                            .getResources().getColor(R.color.black));
                    ((TextView) ((Activity) getContext())
                            .findViewById(R.id.lbl_center))
                            .setTextColor(getResources().getColor(R.color.white));
                    mConfigViewCallback.changeMenuTextColor();
                }
                break;
            default:
                break;
        }
    }

    /**
     * This method is needed to calculate the auto scroll
     * when the user slide the view to the max limit, this
     * starts a animation to finish the view.
     */
    @Override
    public void computeScroll() {
        if (!isInEditMode() && mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Configure the DragViewHelper instance adding a
     * instance of ViewDragHelperCallback, useful to
     * detect the touch callbacks from dragView.
     */
    private void configDragViewHelper() {
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY,
                new ConfigViewHelperCallback(this));
    }

    private boolean smoothSlideTo(View view, int x, int y) {
        if (mViewDragHelper != null && mViewDragHelper.smoothSlideViewTo(view, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    public float getVerticalDragRange() {
        return mVerticalDragRange;
    }

    public void setVerticalDragRange(float mVerticalDragRange) {
        this.mVerticalDragRange = mVerticalDragRange;
    }

    public RelativeLayout getContainer() {
        return mContainer;
    }

    public void setConfigViewCallback(ConfigViewCallback mConfigViewCallback) {
        this.mConfigViewCallback = mConfigViewCallback;
    }

    /**
     * Detect if the mContainer actual position is above the
     * limit determined with the @param dragLimit.
     *
     * @return Use a dimension and compare with the dragged
     * axis position.
     */
    public boolean isDragViewAboveTheLimit() {
        int parentSize = mContainer.getHeight();
        return parentSize < ViewCompat.getY(mContainer) + (parentSize * DEFAULT_DRAG_LIMIT);
    }

    public void moveToOriginalPosition() {
        mConfigViewCallback.showShadow();
        setVisibility(VISIBLE);
        smoothSlideTo(mContainer, 0, 0);
    }

    public void moveOffScreen() {
        smoothSlideTo(mContainer, 0, (int) getVerticalDragRange());
    }

    public void hideView() {
        setVisibility(GONE);
    }

    public void showView() {
        setVisibility(GONE);
    }

    public void onViewPositionChanged(float alpha) {
        mConfigViewCallback.onShadowAlpha(alpha);
    }

}
