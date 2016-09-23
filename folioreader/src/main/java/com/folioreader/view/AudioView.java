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

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.text.Html;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.model.Highlight;
import com.folioreader.smil.AudioElement;
import com.folioreader.util.ViewHelper;

import java.io.IOException;

public class AudioView extends FrameLayout implements View.OnClickListener {

    private static final float SENSITIVITY = 1.0f;
    private static final float DEFAULT_DRAG_LIMIT = 0.5f;
    private static final int INVALID_POINTER = -1;
    private int activePointerId = INVALID_POINTER;

    private ImageButton mPlayPauseBtn;
    private RelativeLayout mContainer;
    private StyleableTextView mHalfSpeed, mOneSpeed, mTwoSpeed, mOneAndHalfSpeed;
    private StyleableTextView mBackgroundColorStyle, mUnderlineStyle, mTextColorStyle;
    private ViewDragHelper mViewDragHelper;

    private Context mContext;
    private FolioActivity mFolioActivity;
    private MediaPlayer mPlayer;
    private AudioElement mAudioElement;
    private Runnable mHighlightTask;
    private ConfigViewCallback mConfigViewCallback;
    private Handler mHandler;

    private int mEnd;
    private int mPosition = 0;
    private String mHighlightStyle;
    private float mVerticalDragRange;

    private boolean mManualPlay = false;

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void inflateView() {
        inflate(getContext(), R.layout.view_audio_player, this);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        initViews();
    }

    private void initViews() {
        mHalfSpeed = (StyleableTextView) findViewById(R.id.btn_half_speed);
        mOneSpeed = (StyleableTextView) findViewById(R.id.btn_one_x_speed);
        mTwoSpeed = (StyleableTextView) findViewById(R.id.btn_twox_speed);
        mOneAndHalfSpeed = (StyleableTextView) findViewById(R.id.btn_one_and_half_speed);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.play_button);
        mBackgroundColorStyle = (StyleableTextView) findViewById(R.id.btn_backcolor_style);
        mBackgroundColorStyle.setSelected(true);
        mUnderlineStyle = (StyleableTextView) findViewById(R.id.btn_text_undeline_style);
        mTextColorStyle = (StyleableTextView) findViewById(R.id.btn_text_color_style);
        mContext = mHalfSpeed.getContext();
        mHighlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
        mOneAndHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.one_and_half_speed)));
        mHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.half_speed_text)));
        mFolioActivity = (FolioActivity) mHalfSpeed.getContext();

        mUnderlineStyle.setText(Html.fromHtml(mHalfSpeed.getContext().getResources().getString(R.string.style_underline)));

        mHighlightTask = new Runnable() {
            @Override
            public void run() {

                int currentPosition = mPlayer.getCurrentPosition();
                if (mPlayer.getDuration() != currentPosition) {
                    if (currentPosition > mEnd) {
                        mAudioElement = mFolioActivity.getElement(mPosition);
                        mEnd = (int) mAudioElement.getClipEnd();
                        mFolioActivity.setHighLight(mPosition, mHighlightStyle);
                        mPosition++;
                    }
                    mHandler.postDelayed(mHighlightTask, 10);
                } else {
                    mHandler.removeCallbacks(mHighlightTask);
                }
            }
        };


        mPlayPauseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        mHalfSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(true);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);
            }
        });

        mOneSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(true);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);

            }
        });
        mOneAndHalfSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(true);
                mTwoSpeed.setSelected(false);

            }
        });
        mTwoSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(true);
            }
        });


        mBackgroundColorStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(true);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(false);
                mHighlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
                mFolioActivity.setHighLightStyle(mHighlightStyle);

            }
        });


        mUnderlineStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(true);
                mTextColorStyle.setSelected(false);
                mHighlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.DottetUnderline);
                mFolioActivity.setHighLightStyle(mHighlightStyle);
            }
        });


        mTextColorStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(true);
                mHighlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.TextColor);
                mFolioActivity.setHighLightStyle(mHighlightStyle);
            }
        });
    }

    public void playerStop() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.release();
            mHandler.removeCallbacks(mHighlightTask);
        }
    }

    public void playerResume() {
        if (mPlayer != null) {
            mPlayer.start();
            mHandler.postDelayed(mHighlightTask, 10);
        }
    }

    private void setUpPlayer() {
        if (mPlayer == null){
            mPlayer = new MediaPlayer();
            try {
                mAudioElement = mFolioActivity.getElement(0);
                String filePath = mAudioElement.getSrc();
                filePath = filePath.substring(2, filePath.length());
                filePath = "/folioreader/temp/OEBPS" + filePath;
                mPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + filePath);
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playAudio() {
        mAudioElement = mFolioActivity.getElement(mPosition);
        setUpPlayer();
        if (mPlayer.isPlaying()){
            mManualPlay = false;
            mPlayer.pause();
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
            mHandler.removeCallbacks(mHighlightTask);
            mFolioActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (mHandler == null) {
                mHandler = new Handler();
            }

            mFolioActivity.setPagerToPosition(mPosition);
            mPlayer.start();
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
            mHandler.post(mHighlightTask);
            mFolioActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            /*mManualPlay = true;
            boolean isPageChange = mFolioActivity.setPagerToPosition(mPosition);
            if (!isPageChange){
                startMediaPlayer();
            }*/
        }
    }

    /*public void startMediaPlayer(){
        if (mManualPlay){
            mPlayer.start();
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
            mHandler.post(mHighlightTask);
        }
    }*/

    /**
     * Bind the attributes of the view and config
     * the DragView with these params.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            inflateView();
            configDragViewHelper();
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
        setmVerticalDragRange(height);
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
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
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
            activePointerId = MotionEventCompat.getPointerId(ev, actionMasked);
        }
        if (activePointerId == INVALID_POINTER) {
            return false;
        }
        mViewDragHelper.processTouchEvent(ev);
        return ViewHelper.isViewHit(mContainer, this, (int) ev.getX(), (int) ev.getY());
    }

    @Override
    public void onClick(View v) {
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
        mViewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new AudioViewHelperCallback(this));
    }

    private boolean smoothSlideTo(View view, int x, int y) {
        if (mViewDragHelper != null && mViewDragHelper.smoothSlideViewTo(view, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    public float getmVerticalDragRange() {
        return mVerticalDragRange;
    }

    public void setmVerticalDragRange(float mVerticalDragRange) {
        this.mVerticalDragRange = mVerticalDragRange;
    }

    public RelativeLayout getmContainer() {
        return mContainer;
    }

    public void setAudioViewCallback(ConfigViewCallback audioViewCallback) {
        this.mConfigViewCallback = audioViewCallback;
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
        smoothSlideTo(mContainer, 0, (int) getmVerticalDragRange());
    }

    public void hideView() {
        setVisibility(GONE);
    }

    public void onViewPositionChanged(float alpha) {
        mConfigViewCallback.onShadowAlpha(alpha);
    }

}
