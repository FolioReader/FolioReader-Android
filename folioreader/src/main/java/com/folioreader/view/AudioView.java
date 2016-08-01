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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.model.Highlight;
import com.folioreader.smil.AudioElement;
import com.folioreader.util.ViewHelper;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioView extends FrameLayout implements View.OnClickListener {

    private static final float SENSITIVITY = 1.0f;
    private static final float DEFAULT_DRAG_LIMIT = 0.5f;
    private static final int INVALID_POINTER = -1;
    private MediaPlayer player;
    private ImageButton playpause;
    private AudioElement mAudioElement;
    private int mStart, mEnd;
    private  int mPosition=0;
    private FolioActivity mFolioActivity;
    private Runnable mEndTask;
    private String mHighlightStyle;
    private long beforeSeekstart,afterSeek;
    private int mSeek;

    private int activePointerId = INVALID_POINTER;

    private boolean isNightMode = false;

    private float verticalDragRange;

    private RelativeLayout container;
    private StyleableTextView mHalfSpeed, mOneSpeed, mTwoSpeed, mOneAndHalfSpeed;
    private StyleableTextView mBackgroundColorStyle, mUnderlineStyle, mTextColorStyle;
    private ViewDragHelper viewDragHelper;
    private ConfigViewCallback configViewCallback;
    private Handler mHandler;

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
        container = (RelativeLayout) findViewById(R.id.container);
        initViews();

    }

    private void initViews() {
        mHalfSpeed = (StyleableTextView) findViewById(R.id.btn_half_speed);
        mOneSpeed = (StyleableTextView) findViewById(R.id.btn_one_x_speed);
        mTwoSpeed = (StyleableTextView) findViewById(R.id.btn_twox_speed);
        mOneAndHalfSpeed = (StyleableTextView) findViewById(R.id.btn_one_and_half_speed);
        playpause = (ImageButton) findViewById(R.id.play_button);
        mBackgroundColorStyle = (StyleableTextView) findViewById(R.id.btn_backcolor_style);
        mUnderlineStyle = (StyleableTextView) findViewById(R.id.btn_text_undeline_style);
        mTextColorStyle = (StyleableTextView) findViewById(R.id.btn_text_color_style);

        mOneAndHalfSpeed.setText(Html.fromHtml("1<sup>1</sup>/<sub>2</sub>x"));
        mHalfSpeed.setText(Html.fromHtml("<sup>1</sup>/<sub>2</sub>x"));
        mFolioActivity= (FolioActivity) mHalfSpeed.getContext();

        mUnderlineStyle.setText(Html.fromHtml(mHalfSpeed.getContext().getResources().getString(R.string.style_underline)));

        mEndTask=new Runnable() {
            @Override
            public void run() {

              /*  player.pause();*/
                int currentPosition=player.getCurrentPosition();
                if(player.getDuration()!=currentPosition) {
                    if (currentPosition > mEnd) {
                        mPosition++;
                        mAudioElement = mFolioActivity.getElement(mPosition);
                        mStart = (int) mAudioElement.getClipBegin();
                        mEnd = (int) mAudioElement.getClipEnd();
                        long cuurenMillies = System.currentTimeMillis();
                        Log.d("mposition", mPosition + "");
                        Log.d("current milles", "" + cuurenMillies);
                        mFolioActivity.setHighLight(mPosition, mHighlightStyle);
                    }
                    mHandler.postDelayed(mEndTask, 10);
                } else {
                    mHandler.removeCallbacks(mEndTask);
                }
            }
        };

        setUpPlayer();



        playpause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPosition==0){
                    mAudioElement = mFolioActivity.getElement(mPosition);
                    mStart = (int) mAudioElement.getClipBegin();
                    mEnd = (int) mAudioElement.getClipEnd();
                    configViewCallback.onAudioPlayed();
                }

                if (player.isPlaying()) {
                   player.pause();
                    playpause.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
                    mSeek=player.getDuration();
                    mHandler.removeCallbacks(mEndTask);

                } else {
                    if(mHandler==null) {
                        mHandler = new Handler();
                    }
                    if(mPosition>0) {
                        player.seekTo(mSeek);
                        player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            @Override
                            public void onSeekComplete(MediaPlayer mp) {
                                player.start();
                                mFolioActivity.setHighLight(mPosition, mHighlightStyle);
                            }
                        });
                    } else {
                        player.start();
                        mFolioActivity.setHighLight(mPosition, mHighlightStyle);
                        mHandler.postDelayed(mEndTask, 10);
                        playpause.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
                    }
                }
               // playMp3();
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
                mHighlightStyle= Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Green);

            }
        });


        mUnderlineStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(true);
                mTextColorStyle.setSelected(false);
                mHighlightStyle= Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.DottetUnderline);

            }
        });


        mTextColorStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(true);
                mHighlightStyle= Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.TextColor);

            }
        });


    }

    private void setUpPlayer() {
        player = new MediaPlayer();
        try {
            player.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/audio" + ".mp3");
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
      /*  mFolioActivity= (FolioActivity) mUnderlineStyle.getContext();
        mAudioElement = mFolioActivity.getElement(mPosition);
        mStart = (int) mAudioElement.getClipBegin();
        mEnd = (int) mAudioElement.getClipEnd();
*/
    }

 /* private void configFonts(){
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
  }*/

 /* private void selectFont(int selectedFont){
    if (selectedFont == FONT_ANDADA){
      findViewById(R.id.btn_font_andada).setSelected(true);
      findViewById(R.id.btn_font_lato).setSelected(false);
      findViewById(R.id.btn_font_lora).setSelected(false);
      findViewById(R.id.btn_font_raleway).setSelected(false);
    } else if (selectedFont == FONT_LATO){
      findViewById(R.id.btn_font_andada).setSelected(false);
      findViewById(R.id.btn_font_lato).setSelected(true);
      findViewById(R.id.btn_font_lora).setSelected(false);
      findViewById(R.id.btn_font_raleway).setSelected(false);
    } else if (selectedFont == FONT_LORA){
      findViewById(R.id.btn_font_andada).setSelected(false);
      findViewById(R.id.btn_font_lato).setSelected(false);
      findViewById(R.id.btn_font_lora).setSelected(true);
      findViewById(R.id.btn_font_raleway).setSelected(false);
    } else if (selectedFont == FONT_RALEWAY){
      findViewById(R.id.btn_font_andada).setSelected(false);
      findViewById(R.id.btn_font_lato).setSelected(false);
      findViewById(R.id.btn_font_lora).setSelected(false);
      findViewById(R.id.btn_font_raleway).setSelected(true);
    }

    Config.getConfig().setFont(selectedFont-1);
    if (configViewCallback!=null) configViewCallback.onConfigChange();
  }*/

 /* private void toggleBlackTheme() {

    int day = getResources().getColor(R.color.white);
    int night = getResources().getColor(R.color.night);
    int darkNight = getResources().getColor(R.color.dark_night);
    final int diffNightDark = night - darkNight;

    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
        isNightMode ? night : day, isNightMode ? day : night);
    colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override public void onAnimationUpdate(ValueAnimator animator) {
        int value = (int) animator.getAnimatedValue();
        container.setBackgroundColor(value);
        if (configViewCallback != null) {
          configViewCallback.onBackgroundUpdate(value - diffNightDark);
        }
      }
    });
    colorAnimation.addListener(new Animator.AnimatorListener() {
      @Override public void onAnimationStart(Animator animator) { }
      @Override public void onAnimationEnd(Animator animator) {
        isNightMode = !isNightMode;
        Config.getConfig().setNightMode(isNightMode);
        configViewCallback.onConfigChange();
      }
      @Override public void onAnimationCancel(Animator animator) { }
      @Override public void onAnimationRepeat(Animator animator) { }
    });

    colorAnimation.setDuration(FADE_DAY_NIGHT_MODE);
    colorAnimation.start();
  }*/

 /* private void configSeekbar(){
    fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Config.getConfig().setFontSize(progress);
        if (audioViewCallback!=null) configViewCallback.onConfigChange();
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
    fontSizeSeekBar.setProgress(Config.getConfig().getFontSize());
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
      /*configFonts();
      configSeekbar();*/
            configDragViewHelper();
     /* selectFont(Config.getConfig().getFont());
      isNightMode = Config.getConfig().isNightMode();
      if (isNightMode){
        dayButton.setSelected(false);
        nightButton.setSelected(true);
      } else {
        dayButton.setSelected(true);
        nightButton.setSelected(false);
      }*/
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
        if (container != null) {
            container.measure(measureWidth, measureHeight);
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
                viewDragHelper.cancel();
                return false;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                activePointerId = MotionEventCompat.getPointerId(ev, index);
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
            default:
                return viewDragHelper.shouldInterceptTouchEvent(ev);
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
        viewDragHelper.processTouchEvent(ev);
        return ViewHelper.isViewHit(container, this, (int) ev.getX(), (int) ev.getY());
    }

    @Override
    public void onClick(View v) {
   /* switch (((Integer) v.getTag())) {
      case Tags.DAY_BUTTON:
        if (isNightMode) {
          isNightMode = true;
          toggleBlackTheme();
          dayButton.setSelected(true);
          nightButton.setSelected(false);
          ((Activity)getContext()).findViewById(R.id.toolbar).setBackgroundColor(getContext().getResources().getColor(R.color.white));
          ((TextView)((Activity)getContext()).findViewById(R.id.lbl_center)).setTextColor(getResources().getColor(R.color.black));
          configViewCallback.changeMenuTextColor();
        }
        break;
      case Tags.NIGHT_BUTTON:
        if (!isNightMode) {
          isNightMode = false;
          toggleBlackTheme();
          dayButton.setSelected(false);
          nightButton.setSelected(true);
          ((Activity)getContext()).findViewById(R.id.toolbar).setBackgroundColor(getContext().getResources().getColor(R.color.black));
          ((TextView)((Activity)getContext()).findViewById(R.id.lbl_center)).setTextColor(getResources().getColor(R.color.white));
          configViewCallback.changeMenuTextColor();
        }
        break;
      default:
        break;
    }*/
    }

    /**
     * This method is needed to calculate the auto scroll
     * when the user slide the view to the max limit, this
     * starts a animation to finish the view.
     */
    @Override
    public void computeScroll() {
        if (!isInEditMode() && viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Configure the DragViewHelper instance adding a
     * instance of ViewDragHelperCallback, useful to
     * detect the touch callbacks from dragView.
     */
    private void configDragViewHelper() {
        viewDragHelper = ViewDragHelper.create(this, SENSITIVITY, new AudioViewHelperCallback(this));
    }

    private boolean smoothSlideTo(View view, int x, int y) {
        if (viewDragHelper != null && viewDragHelper.smoothSlideViewTo(view, x, y)) {
            ViewCompat.postInvalidateOnAnimation(this);
            return true;
        }
        return false;
    }

    public float getVerticalDragRange() {
        return verticalDragRange;
    }

    public void setVerticalDragRange(float verticalDragRange) {
        this.verticalDragRange = verticalDragRange;
    }

    public RelativeLayout getContainer() {
        return container;
    }

    public void setAudioViewCallback(ConfigViewCallback audioViewCallback) {
        this.configViewCallback = audioViewCallback;
    }

    /**
     * Detect if the container actual position is above the
     * limit determined with the @param dragLimit.
     *
     * @return Use a dimension and compare with the dragged
     * axis position.
     */
    public boolean isDragViewAboveTheLimit() {
        int parentSize = container.getHeight();
        return parentSize < ViewCompat.getY(container) + (parentSize * DEFAULT_DRAG_LIMIT);
    }

    public void moveToOriginalPosition() {
        Log.d("dddd","this is executed");
        configViewCallback.showShadow();
        setVisibility(VISIBLE);
        smoothSlideTo(container, 0, 0);
    }

    public void moveOffScreen() {
        smoothSlideTo(container, 0, (int) getVerticalDragRange());
    }

    public void hideView() {
        setVisibility(GONE);
    }

    public void onViewPositionChanged(float alpha) {
        configViewCallback.onShadowAlpha(alpha);
    }


   /* private void adjustAudioLinks() {
        for (int i = 0; i < audio.length; i++)
            for (int j = 0; j < audio[i].length; j++) {
                if (audio[i][j].startsWith("./"))
                    audio[i][j] = currentPage.substring(0,
                            currentPage.lastIndexOf("/"))
                            + audio[i][j].substring(1);

                if (audio[i][j].startsWith("../")) {
                    String temp = currentPage.substring(0,
                            currentPage.lastIndexOf("/"));
                    audio[i][j] = temp.substring(0, temp.lastIndexOf("/"))
                            + audio[i][j].substring(2);
                }
            }
    }*/


    public void playMp3(){
        int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = 512;
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/audio" + ".mp3";

        int i = 0;
        byte[] s = new byte[bufferSize];
        try {
            FileInputStream fin = new FileInputStream(filepath);
            DataInputStream dis = new DataInputStream(fin);

            at.play();
            while((i = dis.read(s, 0, bufferSize)) > -1){
                at.write(s, 0, i);

            }
            at.stop();
            at.release();
            dis.close();
            fin.close();

        } catch (FileNotFoundException e) {
            // TODO
            e.printStackTrace();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

}
