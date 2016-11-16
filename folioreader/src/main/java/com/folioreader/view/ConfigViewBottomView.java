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
import android.support.v4.widget.ViewDragHelper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.util.Tags;

/**
 * Created by mobisys2 on 11/16/2016.
 */

public class ConfigViewBottomView extends BottomSheetDialogFragment implements View.OnClickListener{

    private static final int FONT_ANDADA = 1;
    private static final int FONT_LATO = 2;
    private static final int FONT_LORA = 3;
    private static final int FONT_RALEWAY = 4;
    private static final int FADE_DAY_NIGHT_MODE = 500;

    private CoordinatorLayout.Behavior mBehavior;
    private boolean mIsNightMode = false;

    private float mVerticalDragRange;

    private RelativeLayout mContainer;
    private ImageButton mDayButton;
    private ImageButton mNightButton;
    private SeekBar mFontSizeSeekBar;
    private ViewDragHelper mViewDragHelper;
    private ConfigViewCallback mConfigViewCallback;
    private Dialog mDialog;

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
        mDialog=dialog;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        mBehavior = params.getBehavior();

        if (mBehavior != null && mBehavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) mBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        inflateView();
        initViews();
    }

    private void initViews() {
        inflateView();
        configFonts();
        configSeekbar();
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

    private void inflateView() {
        mContainer = (RelativeLayout) mDialog.findViewById(R.id.container);
        mFontSizeSeekBar = (SeekBar) mDialog.findViewById(R.id.seekbar_font_size);
        mDayButton = (ImageButton) mDialog.findViewById(R.id.day_button);
        mNightButton = (ImageButton) mDialog.findViewById(R.id.night_button);
        mDayButton.setTag(Tags.DAY_BUTTON);
        mNightButton.setTag(Tags.NIGHT_BUTTON);
        mDayButton.setOnClickListener(this);
        mNightButton.setOnClickListener(this);
        mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(true);
    }


    private void configFonts() {
        mDialog.findViewById(R.id.btn_font_andada).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_ANDADA);
            }
        });

        mDialog.findViewById(R.id.btn_font_lato).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_LATO);
            }
        });

        mDialog.findViewById(R.id.btn_font_lora).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_LORA);
            }
        });

        mDialog.findViewById(R.id.btn_font_raleway).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFont(FONT_RALEWAY);
            }
        });


        mDialog.findViewById(R.id.btn_horizontal_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfigViewCallback.onOrentationChange(1);
                mDialog.findViewById(R.id.btn_horizontal_orentation).setSelected(true);
                mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(false);
            }
        });

        mDialog.findViewById(R.id.btn_vertical_orentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfigViewCallback.onOrentationChange(0);
                mDialog.findViewById(R.id.btn_horizontal_orentation).setSelected(false);
                mDialog.findViewById(R.id.btn_vertical_orentation).setSelected(true);
            }
        });
    }

    private void selectFont(int selectedFont) {
        if (selectedFont == FONT_ANDADA) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(true);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_LATO) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(true);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_LORA) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(true);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(false);
        } else if (selectedFont == FONT_RALEWAY) {
            mDialog.findViewById(R.id.btn_font_andada).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lato).setSelected(false);
            mDialog.findViewById(R.id.btn_font_lora).setSelected(false);
            mDialog.findViewById(R.id.btn_font_raleway).setSelected(true);
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
}
