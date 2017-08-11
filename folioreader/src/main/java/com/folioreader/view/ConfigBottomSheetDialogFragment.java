package com.folioreader.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.model.event.ReloadDataEvent;


/**
 * Created by mobisys2 on 11/16/2016.
 */

public class ConfigBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public static final int DAY_BUTTON = 30;
    public static final int NIGHT_BUTTON = 31;
    private static final int FADE_DAY_NIGHT_MODE = 500;

    private CoordinatorLayout.Behavior mBehavior;
    private boolean mIsNightMode = false;


    private RelativeLayout mContainer;
    private ImageButton mDayButton;
    private ImageButton mNightButton;
    private SeekBar mFontSizeSeekBar;
    private Dialog mDialog;
    private ConfigDialogCallback mConfigDialogCallback;

    public interface ConfigDialogCallback {
        void onOrientationChange(int orentation);
    }

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
        View contentView = View.inflate(getContext(), R.layout.view_config, null);
        dialog.setContentView(contentView);
        mDialog = dialog;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        mBehavior = params.getBehavior();

        if (mBehavior != null && mBehavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) mBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        //if (mConfigDialogCallback != null) mConfigDialogCallback.onConfigChange();
        initViews();
    }

    private void initViews() {
        inflateView();
        configFonts();
        mFontSizeSeekBar.setProgress(Config.getConfig().getFontSize());
        configSeekbar();
        selectFont(Config.getConfig().getFont(), false);
        mIsNightMode = Config.getConfig().isNightMode();
        if (mIsNightMode) {
            mContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.night));
        } else {
            mContainer.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        }

        if (mIsNightMode) {
            mDayButton.setSelected(false);
            mNightButton.setSelected(true);
        } else {
            mDayButton.setSelected(true);
            mNightButton.setSelected(false);
        }

        mConfigDialogCallback = (ConfigDialogCallback) getActivity();
    }

    private void inflateView() {
        mContainer = (RelativeLayout) mDialog.findViewById(R.id.container);
        mFontSizeSeekBar = (SeekBar) mDialog.findViewById(R.id.seekbar_font_size);
        mDayButton = (ImageButton) mDialog.findViewById(R.id.day_button);
        mNightButton = (ImageButton) mDialog.findViewById(R.id.night_button);
        mDayButton.setTag(DAY_BUTTON);
        mNightButton.setTag(NIGHT_BUTTON);
        mDayButton.setOnClickListener(this);
        mNightButton.setOnClickListener(this);
        mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(true);
    }


    private void configFonts() {
        mDialog.findViewById(R.id.btn_font_andada).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_ANDADA, true);
            }
        });

        mDialog.findViewById(R.id.btn_font_lato).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_LATO, true);
            }
        });

        mDialog.findViewById(R.id.btn_font_lora).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_LORA, true);
            }
        });

        mDialog.findViewById(R.id.btn_font_raleway).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(Constants.FONT_RALEWAY, true);
            }
        });


        mDialog.findViewById(R.id.btn_horizontal_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfigDialogCallback.onOrientationChange(1);
                mDialog.findViewById(R.id.btn_horizontal_orentation).setSelected(true);
                mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(false);
            }
        });

        mDialog.findViewById(R.id.btn_vertical_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfigDialogCallback.onOrientationChange(0);
                mDialog.findViewById(R.id.btn_horizontal_orentation).setSelected(false);
                mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(true);
            }
        });
    }

    private void selectFont(int selectedFont, boolean isReloadNeeded) {
        if (selectedFont == Constants.FONT_ANDADA) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(true);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == Constants.FONT_LATO) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(true);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == Constants.FONT_LORA) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(true);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == Constants.FONT_RALEWAY) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(true);
        }

        Config.getConfig().setFont(selectedFont);
        //if (mConfigDialogCallback != null) mConfigDialogCallback.onConfigChange();
        if (isAdded() && isReloadNeeded) {
            FolioActivity.BUS.post(new ReloadDataEvent());
        }
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
                FolioActivity.BUS.post(new ReloadDataEvent());
                ///mConfigDialogCallback.onConfigChange();
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
                //if (mConfigViewCallback != null) mConfigViewCallback.onConfigChange();
                //if (mConfigDialogCallback != null) mConfigDialogCallback.onConfigChange();
                FolioActivity.BUS.post(new ReloadDataEvent());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (((Integer) v.getTag())) {
            case DAY_BUTTON:
                if (mIsNightMode) {
                    mIsNightMode = true;
                    toggleBlackTheme();
                    mDayButton.setSelected(true);
                    mNightButton.setSelected(false);
                    setToolBarColor();
                    setAudioPlayerBackground();
                }
                break;
            case NIGHT_BUTTON:
                if (!mIsNightMode) {
                    mIsNightMode = false;
                    toggleBlackTheme();
                    mDayButton.setSelected(false);
                    mNightButton.setSelected(true);
                    setToolBarColor();
                    setAudioPlayerBackground();
                }
                break;
            default:
                break;
        }
    }

    private void setToolBarColor() {
        if (mIsNightMode) {
            ((Activity) getContext()).
                    findViewById(R.id.toolbar).
                    setBackgroundColor(getContext().getResources().getColor(R.color.white));
            ((TextView) ((Activity) getContext()).
                    findViewById(R.id.lbl_center)).
                    setTextColor(getResources().getColor(R.color.black));
        } else {
            ((Activity) getContext()).
                    findViewById(R.id.toolbar).
                    setBackgroundColor(getContext().getResources().getColor(R.color.black));
            ((TextView) ((Activity) getContext()).
                    findViewById(R.id.lbl_center)).
                    setTextColor(getResources().getColor(R.color.white));
        }

    }

    private void setAudioPlayerBackground() {
        if(mIsNightMode) {
            ((Activity) getContext()).
                    findViewById(R.id.container).
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            ((Activity) getContext()).
                    findViewById(R.id.container).
                    setBackgroundColor(ContextCompat.getColor(getContext(), R.color.night));
        }
    }
}
