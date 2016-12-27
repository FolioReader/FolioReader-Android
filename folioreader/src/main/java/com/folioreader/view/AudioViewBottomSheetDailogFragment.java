package com.folioreader.view;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.model.Highlight;
import com.folioreader.model.RewindIndex;
import com.folioreader.model.Sentence;
import com.folioreader.smil.AudioElement;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.util.UiUtil;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import static android.view.View.GONE;

/**
 * Created by PC on 12/10/2016.
 */

public class AudioViewBottomSheetDailogFragment extends BottomSheetDialogFragment {
    private CoordinatorLayout.Behavior mBehavior;
    private Dialog mDialog;
    private static final String TAG = AudioViewBottomSheetDailogFragment.class.getSimpleName();

    private ImageButton mPlayPauseBtn;
    private StyleableTextView mHalfSpeed, mOneSpeed, mTwoSpeed, mOneAndHalfSpeed;
    private StyleableTextView mBackgroundColorStyle, mUnderlineStyle, mTextColorStyle;
    private boolean mUnderlineStyleIsSelected, mTextColorStyleIsSelected;
    private TextToSpeech mTextToSpeech;

    private Context mContext;
    private FolioActivity mFolioActivity;
    private MediaPlayer mPlayer;
    private AudioElement mAudioElement;
    private Runnable mHighlightTask;
    private Handler mHandler;

    private int mEnd;
    private int mPosition = 0;
    private String mHighlightStyle;
    private boolean isRegistered;


    private boolean mIsSpeaking = false;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.view_audio_player, null);
        dialog.setContentView(contentView);
        mDialog = dialog;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        mBehavior = params.getBehavior();

        if (mBehavior != null && mBehavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) mBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        initViews();
        if (!isRegistered) {
            FolioActivity.BUS.register(this);
            isRegistered = true;
        }

    }


    private void initViews() {
        mHalfSpeed = (StyleableTextView) mDialog.findViewById(R.id.btn_half_speed);
        mOneSpeed = (StyleableTextView) mDialog.findViewById(R.id.btn_one_x_speed);
        mTwoSpeed = (StyleableTextView) mDialog.findViewById(R.id.btn_twox_speed);
        mOneAndHalfSpeed = (StyleableTextView) mDialog.findViewById(R.id.btn_one_and_half_speed);
        mPlayPauseBtn = (ImageButton) mDialog.findViewById(R.id.play_button);
        mBackgroundColorStyle = (StyleableTextView) mDialog.findViewById(R.id.btn_backcolor_style);
        mUnderlineStyle = (StyleableTextView) mDialog.findViewById(R.id.btn_text_undeline_style);
        mTextColorStyle = (StyleableTextView) mDialog.findViewById(R.id.btn_text_color_style);
        mContext = mHalfSpeed.getContext();
        mHighlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
        mOneAndHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.one_and_half_speed)));
        mHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.half_speed_text)));
        mFolioActivity = (FolioActivity) mHalfSpeed.getContext();
        String styleUnderline =
                mHalfSpeed.getContext().getResources().getString(R.string.style_underline);
        mUnderlineStyle.setText(Html.fromHtml(styleUnderline));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mDialog.findViewById(R.id.playback_speed_Layout).setVisibility(GONE);
        }

        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
        } else if (mTextToSpeech != null && mTextToSpeech.isSpeaking()) {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
        }

        if (mTextColorStyleIsSelected) {
            mTextColorStyle.setSelected(true);
        } else if (mUnderlineStyleIsSelected) {
            mUnderlineStyle.setSelected(true);
        } else {
            mBackgroundColorStyle.setSelected(true);
        }


        mHighlightTask = new Runnable() {
            @Override
            public void run() {

                int currentPosition = mPlayer.getCurrentPosition();
                if (mPlayer.getDuration() != currentPosition) {
                    if (currentPosition > mEnd) {
                        mAudioElement = mFolioActivity.getElement(mPosition);
                        mEnd = (int) mAudioElement.getClipEnd();
                        //if(isAdded()) {
                        FolioActivity.BUS.post(mPosition);
                        //mFolioActivity.setHighLight(mPosition);
                        // }
                        mPosition++;
                    }
                    mHandler.postDelayed(mHighlightTask, 10);
                } else {
                    mHandler.removeCallbacks(mHighlightTask);
                }
            }
        };

        mTextToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.UK);
                    mTextToSpeech.setSpeechRate(0.70f);
                }

                mTextToSpeech.setOnUtteranceCompletedListener(
                        new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {
                                mFolioActivity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (mIsSpeaking) {
                                            FolioActivity.BUS.post(true);
                                            //mFolioActivity.getSentance();
                                        }
                                    }
                                });
                            }
                        });
            }
        });


        mPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFolioActivity.isSmilAvailable()) {
                    playAudio();
                } else {
                    playAudioWithoutSmil();
                }
            }
        });

        mHalfSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(true);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);
                mPlayer.getPlaybackParams().setSpeed(100.7f);
            }
        });

        mOneSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(true);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);

            }
        });
        mOneAndHalfSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(true);
                mTwoSpeed.setSelected(false);

            }
        });
        mTwoSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(true);
            }
        });


        mBackgroundColorStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(true);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(false);
                mHighlightStyle =
                        Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
                //mFolioActivity.setHighLightStyle(mHighlightStyle);
                FolioActivity.BUS.post(mHighlightStyle);
                mUnderlineStyleIsSelected = false;
                mTextColorStyleIsSelected = false;

            }
        });


        mUnderlineStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(true);
                mTextColorStyle.setSelected(false);
                mHighlightStyle =
                        Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.DottetUnderline);
                FolioActivity.BUS.post(mHighlightStyle);
                mUnderlineStyleIsSelected = true;
                mTextColorStyleIsSelected = false;
            }
        });


        mTextColorStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(true);
                mHighlightStyle =
                        Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.TextColor);
                FolioActivity.BUS.post(mHighlightStyle);
                mTextColorStyleIsSelected = true;
                mUnderlineStyleIsSelected = false;
            }
        });
    }


    private void playAudioWithoutSmil() {
        if (mTextToSpeech.isSpeaking()) {
            mTextToSpeech.stop();
            mIsSpeaking = false;
            FolioActivity.BUS.post(new RewindIndex());
            //mFolioActivity.resetCurrentIndex();
            UiUtil.keepScreenAwake(false, mContext);
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
        } else {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
            mIsSpeaking = true;
            UiUtil.keepScreenAwake(true, mContext);
            if (isAdded()) {
                FolioActivity.BUS.post(true);
            }

        }
    }

    @Subscribe
    public void speakAudio(Sentence sentence) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");
        mTextToSpeech.speak(sentence.getSentence(), TextToSpeech.QUEUE_FLUSH, params);
    }

    public void playerStop() {
        mIsSpeaking = false;
        if (mFolioActivity.isSmilAvailable()) {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.release();
                mHandler.removeCallbacks(mHighlightTask);
            }
        } else {
            if (mTextToSpeech != null) {
                mTextToSpeech.stop();
                mTextToSpeech.shutdown();
            }
        }
    }

    public void playerResume() {
        if (mPlayer != null) {
            mPlayer.start();
            mHandler.postDelayed(mHighlightTask, 10);
        }
    }

    private void setUpPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            try {
                mAudioElement = mFolioActivity.getElement(0);
                String filePath = mAudioElement.getSrc();
                String folderPath =
                        FileUtil.getFolioEpubFolderPath(mFolioActivity.getEpubFileName());
                filePath = filePath.substring(2, filePath.length());
                String opfpath
                        = AppUtil.getPathOPF(
                        FileUtil.getFolioEpubFolderPath(mFolioActivity.getEpubFileName()),
                        mFolioActivity);
                filePath = folderPath + "/" + opfpath + "/" + filePath;
                mPlayer.setDataSource(filePath);
                mPlayer.prepare();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void playAudio() {
        mAudioElement = mFolioActivity.getElement(mPosition);
        setUpPlayer();
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.play_icon));
            mHandler.removeCallbacks(mHighlightTask);
            UiUtil.keepScreenAwake(false, mContext);
            //mFolioActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            if (mHandler == null) {
                mHandler = new Handler();
            }

            mFolioActivity.setPagerToPosition(mPosition);
            mPlayer.start();
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.pause_btn));
            mHandler.post(mHighlightTask);
            UiUtil.keepScreenAwake(true, mContext);
            //mFolioActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    public void unRegisterBus() {
        if(isRegistered) {
            FolioActivity.BUS.unregister(this);
        }
    }

    public void stopAudioIfPlaying() {
       if(mPlayer!=null){
           mPlayer.stop();
       } else if(mTextToSpeech!=null){
           mTextToSpeech.shutdown();
       }
    }
}




