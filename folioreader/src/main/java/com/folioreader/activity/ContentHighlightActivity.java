package com.folioreader.activity;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ContentFrameLayout;
import android.view.View;
import android.widget.TextView;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.fragments.ContentsFragment;
import com.folioreader.fragments.HighlightListFragment;
import com.folioreader.model.Highlight;

import nl.siegmann.epublib.domain.Book;

public class ContentHighlightActivity extends AppCompatActivity {
    private Book mBook;
    private int mSelectedChapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mBook = (Book) getIntent().getSerializableExtra(Constants.BOOK);
        mSelectedChapterPosition = getIntent().getIntExtra(Constants.SELECTED_CHAPTER_POSITION, 0);
        setContentView(R.layout.activity_content_highlight);
        initViews();
    }

    private void initViews() {
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
        ContentsFragment contentFrameLayout = ContentsFragment.newInstance(mBook, mSelectedChapterPosition);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.parent, contentFrameLayout);
        ft.commit();
    }

    private void loadHighlightsFragment() {
        findViewById(R.id.btn_contents).setSelected(false);
        findViewById(R.id.btn_highlights).setSelected(true);
        HighlightListFragment highlightListFragment = HighlightListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.parent, highlightListFragment);
        ft.commit();
    }
}
