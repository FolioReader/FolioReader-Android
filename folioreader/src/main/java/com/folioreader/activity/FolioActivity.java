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
package com.folioreader.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.adapter.FolioPageFragmentAdapter;
import com.folioreader.adapter.TOCAdapter;
import com.folioreader.fragments.FolioPageFragment;
import com.folioreader.model.Highlight;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;
import com.folioreader.view.VerticalViewPager;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
        FolioViewCallback, FolioPageFragment.FolioPageFragmentCallback, TOCAdapter.ChapterSelectionCallBack {

    public static final String INTENT_EPUB_ASSET_PATH = "com.folioreader.epub_asset_path";
    public static final int ACTION_HIGHLIGHT_lIST = 77;
    private static final String HIGHLIGHT_ITEM ="highlight_item" ;

    private RecyclerView recyclerViewMenu;
    private VerticalViewPager mFolioPageViewPager;
    private FolioView folioView;
    private ConfigView configView;
    private Toolbar mToolbar;

    private String mEpubAssetPath;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences = new ArrayList<>();
    private List<SpineReference> mSpineReferences;
    private List<String> mSpineReferenceHtmls = new ArrayList<>();
    private boolean mIsActionBarVisible = false;
    private TOCAdapter mTocAdapter;
    private int mChapterPosition;
    private ActionMode mActionMode = null;

    private boolean mHighLight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);

        mEpubAssetPath = getIntent().getStringExtra(INTENT_EPUB_ASSET_PATH);
        loadBook();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        findViewById(R.id.btn_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.drawer_menu);
                if (!((DrawerLayout) findViewById(R.id.drawer_left)).isDrawerOpen(relativeLayout)) {
                    ((DrawerLayout) findViewById(R.id.drawer_left)).openDrawer(relativeLayout);
                } else {
                    ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer(relativeLayout);
                }
            }
        });
    }

    private void loadBook() {
        AssetManager assetManager = getAssets();
        try {
            InputStream epubInputStream = assetManager.open(mEpubAssetPath);
            mBook = (new EpubReader()).readEpub(epubInputStream);
            populateTableOfContents(mBook.getTableOfContents().getTocReferences(), 0);
            Spine spine = new Spine(mBook.getTableOfContents());
            this.mSpineReferences = spine.getSpineReferences();
            for (int i = 0; i < mSpineReferences.size(); i++) mSpineReferenceHtmls.add(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateTableOfContents(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }

        for (TOCReference tocReference : tocReferences) {
            mTocReferences.add(tocReference);
            populateTableOfContents(tocReference.getChildren(), depth + 1);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        recyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
        folioView = (FolioView) findViewById(R.id.folio_view);
        configView = (ConfigView) findViewById(R.id.config_view);
        configRecyclerViews();
        configFolio();
        configDrawerLayoutButtons();
    }

    @Override
    public void onBackPressed() {
        if (configView.isDragViewAboveTheLimit()) {
            configView.moveToOriginalPosition();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackgroundUpdate(int value) {
        recyclerViewMenu.setBackgroundColor(value);
        folioView.setBackgroundColor(value);

    }

    @Override
    public void changeMenuTextColor() {
        mTocAdapter.setNightMode(!Config.getConfig().isNightMode());
        mTocAdapter.setSelectedChapterPosition(mChapterPosition);
        mTocAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShadowAlpha(float alpha) {
        folioView.updateShadowAlpha(alpha);
    }

    @Override
    public void showShadow() {
        folioView.resetView();
    }

    @Override
    public void onConfigChange() {

        int position = mFolioPageViewPager.getCurrentItem();
        //reload previous, current and next fragment
        Fragment page;
        if (position != 0) {
            page = getFragment(position - 1);
            ((FolioPageFragment) page).reload();
        }
        page = getFragment(position);
        ((FolioPageFragment) page).reload();
        if (position < mSpineReferences.size()) {
            page = getFragment(position + 1);
            ((FolioPageFragment) page).reload();
        }
    }

    private Fragment getFragment(int pos) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.folioPageViewPager + ":" + (pos));
    }

    @Override
    public void onShadowClick() {
        configView.moveOffScreen();
    }

    private void configRecyclerViews() {
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        if (mTocReferences != null) {
            mTocAdapter = new TOCAdapter(mTocReferences, FolioActivity.this);
            recyclerViewMenu.setAdapter(mTocAdapter);
        }
    }

    private void configFolio() {
        mFolioPageViewPager = (VerticalViewPager) folioView.findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChapterPosition = position;
                mTocAdapter.setNightMode(Config.getConfig().isNightMode());
                mTocAdapter.setSelectedChapterPosition(mChapterPosition);
                mTocAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mBook != null && mSpineReferences != null) {
            FolioPageFragmentAdapter folioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(), mSpineReferences, mBook);
            mFolioPageViewPager.setAdapter(folioPageFragmentAdapter);
        }

        folioView.setFolioViewCallback(this);
        configView.setConfigViewCallback(this);
    }

    private void configDrawerLayoutButtons() {
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawers();
                finish();
            }
        });
        findViewById(R.id.btn_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawers();
                if (configView.isDragViewAboveTheLimit()) {
                    configView.moveToOriginalPosition();
                } else {
                    configView.moveOffScreen();
                }
            }
        });

        findViewById(R.id.btn_highlight_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer((RelativeLayout) findViewById(R.id.drawer_menu));
                Intent intent = new Intent(FolioActivity.this, HighlightListActivity.class);
                startActivityForResult(intent,ACTION_HIGHLIGHT_lIST);
            }
        });
    }

    @Override
    public String getChapterHtmlContent(int position) {
        return reader(position);
    }

    @Override
    public void hideOrshowToolBar() {
        Log.d("in hideOrshowToolBar","main");
        if (mIsActionBarVisible)
            toolbarAnimateHide();
        else {
            toolbarAnimateShow(1);
        }
    }

    @Override
    public void hideToolBarIfVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        }
    }

    @Override
    public void invalidateActionMode() {
    }

    private String reader(int position) {
        if (mSpineReferenceHtmls.get(position) != null) {
            return mSpineReferenceHtmls.get(position);
        } else {
            try {
                Reader reader = mSpineReferences.get(position).getResource().getReader();

                StringBuilder builder = new StringBuilder();
                int numChars;
                char[] cbuf = new char[2048];
                while ((numChars = reader.read(cbuf)) >= 0) {
                    builder.append(cbuf, 0, numChars);
                }
                String content = builder.toString();
                mSpineReferenceHtmls.set(position, content);
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }


    private void toolbarAnimateShow(final int verticalOffset) {
        mToolbar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        toolbarSetElevation(verticalOffset == 0 ? 0 : 1);
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsActionBarVisible) {
                            toolbarAnimateHide();
                        }
                    }
                });
            }
        }, 5000);

        mIsActionBarVisible = true;
    }

    private void toolbarAnimateHide() {
        mToolbar.animate()
                .translationY(-mToolbar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolbarSetElevation(0);
                    }
                });
        mIsActionBarVisible = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toolbarSetElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(elevation);
        }
    }

    @Override
    public void onChapterSelect(int position) {
        mFolioPageViewPager.setCurrentItem(position);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.drawer_menu);
        ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer(relativeLayout);
    }

    private String getSelectedTextFromPage() {
        int currentPosition = mFolioPageViewPager.getCurrentItem();
        FolioPageFragment folioPageFragment = (FolioPageFragment) getFragment(currentPosition);
        String selectedText = folioPageFragment.getSelectedText();
        return selectedText;
    }

    public Highlight setCurrentPagerPostion(Highlight highlight){
        highlight.setCurrentPagerPostion(mFolioPageViewPager.getCurrentItem());
        return  highlight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==ACTION_HIGHLIGHT_lIST && resultCode==RESULT_OK){
            if(data.hasExtra(HIGHLIGHT_ITEM)) {
                Highlight highlight= data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position=highlight.getCurrentPagerPostion();
                mFolioPageViewPager.setCurrentItem(position);
                Fragment fragment=getFragment(position);
                ((FolioPageFragment) fragment).setWebViewPosition(highlight.getCurrentWebviewScrollPos());

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*@Override
    public void onActionModeStarted(ActionMode mode) {
        String selectedText = getSelectedTextFromPage();
        if (!TextUtils.isEmpty(selectedText)) {
            if (mActionMode == null) {
                mActionMode = mode;
                Menu menu = mode.getMenu();
                menu.clear();

                // Inflate your own menu items
                mode.getMenuInflater().inflate(R.menu.menu_text_selection, menu);
            }
        }


        super.onActionModeStarted(mode);
    }

    public void onContextualMenuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.menu_copy) {
            AppUtil.copyToClipboard(this, getSelectedTextFromPage());
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_define) {
            Toast.makeText(this, "Define " + getSelectedTextFromPage(), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_highlight) {
            mHighLight = true;
            int currentPosition = mFolioPageViewPager.getCurrentItem();
            FolioPageFragment folioPageFragment = (FolioPageFragment) getFragment(currentPosition);
            folioPageFragment.highlight();
            startSupportActionMode(mActionModeCallback);
        } else if (item.getItemId() == R.id.menu_share) {
            AppUtil.share(this, getSelectedTextFromPage());
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        mActionMode = null;
        super.onActionModeFinished(mode);
    }

<<<<<<< HEAD
    private android.support.v7.view.ActionMode.Callback mActionModeCallback = new android.support.v7.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            Log.d("FolioActivity", "onCreateActionMode() ========>");
            // Inflate your own menu items
            menu.clear();
            mode.getMenuInflater().inflate(R.menu.menu_on_highlight, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            Log.d("FolioActivity", "onPrepareActionMode() ========>");
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
            Log.d("FolioActivity", "onActionItemClicked() ========>");
            return false;
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            Log.d("FolioActivity", "onDestroyActionMode() ========>");
        }
    };*/


}
