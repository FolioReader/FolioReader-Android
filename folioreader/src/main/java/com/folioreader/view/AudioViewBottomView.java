package com.folioreader.view;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.folioreader.R;

/**
 * Created by mobisys2 on 11/16/2016.
 */

public class AudioViewBottomView extends BottomSheetDialogFragment {
    private CoordinatorLayout.Behavior mBehavior;

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

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        mBehavior = params.getBehavior();

        if( mBehavior != null && mBehavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) mBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    private void initViews(){

    }
}
