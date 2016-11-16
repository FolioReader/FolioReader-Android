package com.folioreader.activity;

import android.app.Dialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.fragments.ContentsFragment;
import com.folioreader.fragments.HighlightListFragment;
import com.folioreader.util.AppUtil;
import com.folioreader.util.ScreenUtils;

import nl.siegmann.epublib.domain.Book;

public class ContentHighlightBottomSheet extends BottomSheetDialogFragment {
    private Book mBook;
    private int mSelectedChapterPosition;
    private boolean mIsNightMode;
    private CoordinatorLayout.Behavior mBehavior;
    private Dialog mDialog;

  /*  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_highlight);
        getSupportActionBar().hide();
        mBook = (Book) getIntent().getSerializableExtra(Constants.BOOK);
        mSelectedChapterPosition = getIntent().getIntExtra(Constants.SELECTED_CHAPTER_POSITION, 0);
        mIsNightMode = Config.getConfig().isNightMode();
        initViews();
    }*/

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

    private void initViews() {
        if (mIsNightMode) {
            ((Toolbar) mDialog.findViewById(R.id.toolbar)).setBackgroundColor(Color.BLACK);
            ((TextView) mDialog.findViewById(R.id.btn_contents))
                    .setBackgroundResource(R.drawable.content_highlight_back_selector_night_mode);
            ((TextView) mDialog.findViewById(R.id.btn_contents))
                    .setTextColor(AppUtil.getColorList(getActivity(), R.color.black, R.color.app_green));
            ((TextView) mDialog.findViewById(R.id.btn_highlights))
                    .setBackgroundResource(R.drawable.content_highlight_back_selector_night_mode);
            ((TextView) mDialog.findViewById(R.id.btn_highlights))
                    .setTextColor(AppUtil.getColorList(getActivity(), R.color.black, R.color.app_green));
        }

        //loadContentFragment();
        mDialog.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDialog.findViewById(R.id.btn_contents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContentFragment();
            }
        });

        mDialog.findViewById(R.id.btn_highlights).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHighlightsFragment();
            }
        });
    }

    private void loadContentFragment() {
        mDialog.findViewById(R.id.btn_contents).setSelected(true);
        mDialog.findViewById(R.id.btn_highlights).setSelected(false);
        ContentsFragment contentFrameLayout
                = ContentsFragment.newInstance(mBook, mSelectedChapterPosition);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.parent, contentFrameLayout);
        ft.commit();
    }

    private void loadHighlightsFragment() {
        mDialog.findViewById(R.id.btn_contents).setSelected(false);
        mDialog.findViewById(R.id.btn_highlights).setSelected(true);
        HighlightListFragment highlightListFragment = HighlightListFragment.newInstance(mBook);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.parent, highlightListFragment);
        ft.commit();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.activity_content_highlight, null);
        dialog.setContentView(contentView);
        mDialog=dialog;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        mBehavior = params.getBehavior();
        if( mBehavior != null && mBehavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) mBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        ScreenUtils screenUtils=new ScreenUtils(getActivity());
        mBehavior.setPeekHeight(screenUtils.getHeight());

        mBook = (Book) getArguments().getSerializable(Constants.BOOK);
        mSelectedChapterPosition = getArguments().getInt(Constants.SELECTED_CHAPTER_POSITION, 0);
        mIsNightMode = Config.getConfig().isNightMode();
        initViews();
    }
}
