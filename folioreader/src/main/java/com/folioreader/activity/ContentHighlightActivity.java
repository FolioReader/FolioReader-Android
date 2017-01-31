package com.folioreader.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.fragments.ContentsFragment;
import com.folioreader.fragments.HighlightListFragment;
import com.folioreader.util.UiUtil;

public class ContentHighlightActivity extends AppCompatActivity {
    private String mBookTitle;
    private int mSelectedChapterPosition;
    private boolean mIsNightMode;
    private String mBookPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_highlight);
        getSupportActionBar().hide();
        mBookTitle = getIntent().getStringExtra(Constants.BOOK_TITLE);
        mBookPath = getIntent().getStringExtra(Constants.BOOK_FILE_PATH);
        mSelectedChapterPosition = getIntent().getIntExtra(Constants.SELECTED_CHAPTER_POSITION, 0);
        mIsNightMode = Config.getConfig().isNightMode();
        initViews();
    }

    private void initViews() {
        if (mIsNightMode) {
            ((Toolbar) findViewById(R.id.toolbar)).setBackgroundColor(Color.BLACK);
            ((TextView) findViewById(R.id.btn_contents))
                    .setBackgroundResource(R.drawable.content_highlight_back_selector_night_mode);
            ((TextView) findViewById(R.id.btn_contents))
                    .setTextColor(UiUtil.getColorList(ContentHighlightActivity.this, R.color.black, R.color.app_green));
            ((TextView) findViewById(R.id.btn_highlights))
                    .setBackgroundResource(R.drawable.content_highlight_back_selector_night_mode);
            ((TextView) findViewById(R.id.btn_highlights))
                    .setTextColor(UiUtil.getColorList(ContentHighlightActivity.this, R.color.black, R.color.app_green));
        }

        loadContentFragment();
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_contents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContentFragment();
            }
        });

        findViewById(R.id.btn_highlights).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHighlightsFragment();
            }
        });
    }

    private void loadContentFragment() {
        findViewById(R.id.btn_contents).setSelected(true);
        findViewById(R.id.btn_highlights).setSelected(false);
        ContentsFragment contentFrameLayout
                = ContentsFragment.newInstance(mBookPath, mSelectedChapterPosition);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.parent, contentFrameLayout);
        ft.commit();
    }

    private void loadHighlightsFragment() {
        findViewById(R.id.btn_contents).setSelected(false);
        findViewById(R.id.btn_highlights).setSelected(true);
        HighlightListFragment highlightListFragment = HighlightListFragment.newInstance(mBookTitle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.parent, highlightListFragment);
        ft.commit();
    }
}
