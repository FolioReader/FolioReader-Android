package com.folioreader.ui.folio.activity;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.ui.folio.fragment.HighlightFragment;
import com.folioreader.ui.tableofcontents.view.TableOfContentFragment;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;

public class ContentHighlightActivity extends AppCompatActivity {
    private boolean mIsNightMode;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_highlight);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mConfig = AppUtil.getSavedConfig(this);
        mIsNightMode = mConfig != null && mConfig.isNightMode();
        initViews();
    }

    private void initViews() {

        UiUtil.setColorToImage(this, mConfig.getThemeColor(), ((ImageView) findViewById(R.id.btn_close)).getDrawable());
        findViewById(R.id.layout_content_highlights).setBackgroundDrawable(UiUtil.getShapeDrawable(this, mConfig.getThemeColor()));

        if (mIsNightMode) {
            findViewById(R.id.toolbar).setBackgroundColor(Color.BLACK);
            findViewById(R.id.btn_contents).setBackgroundDrawable(UiUtil.convertColorIntoStateDrawable(this, mConfig.getThemeColor(), R.color.black));
            findViewById(R.id.btn_highlights).setBackgroundDrawable(UiUtil.convertColorIntoStateDrawable(this, mConfig.getThemeColor(), R.color.black));
            ((TextView) findViewById(R.id.btn_contents)).setTextColor(UiUtil.getColorList(this, R.color.black, mConfig.getThemeColor()));
            ((TextView) findViewById(R.id.btn_highlights)).setTextColor(UiUtil.getColorList(this, R.color.black, mConfig.getThemeColor()));

        } else {
            ((TextView) findViewById(R.id.btn_contents)).setTextColor(UiUtil.getColorList(this, R.color.white, mConfig.getThemeColor()));
            ((TextView) findViewById(R.id.btn_highlights)).setTextColor(UiUtil.getColorList(this, R.color.white, mConfig.getThemeColor()));
            findViewById(R.id.btn_contents).setBackgroundDrawable(UiUtil.convertColorIntoStateDrawable(this, mConfig.getThemeColor(), R.color.white));
            findViewById(R.id.btn_highlights).setBackgroundDrawable(UiUtil.convertColorIntoStateDrawable(this, mConfig.getThemeColor(), R.color.white));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (mIsNightMode) {
                color = ContextCompat.getColor(this, R.color.black);
            } else {
                int[] attrs = {android.R.attr.navigationBarColor};
                TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);
                color = typedArray.getColor(0, ContextCompat.getColor(this, R.color.white));
            }
            getWindow().setNavigationBarColor(color);
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
        TableOfContentFragment contentFrameLayout
                = TableOfContentFragment.newInstance(getIntent().getStringExtra(Constants.CHAPTER_SELECTED),
                getIntent().getStringExtra(Constants.BOOK_TITLE));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.parent, contentFrameLayout);
        ft.commit();
    }

    private void loadHighlightsFragment() {
        findViewById(R.id.btn_contents).setSelected(false);
        findViewById(R.id.btn_highlights).setSelected(true);
        String bookId = getIntent().getStringExtra(FolioReader.INTENT_BOOK_ID);
        String bookTitle= getIntent().getStringExtra(Constants.BOOK_TITLE);
        HighlightFragment highlightFragment = HighlightFragment.newInstance(bookId, bookTitle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.parent, highlightFragment);
        ft.commit();
    }

}
