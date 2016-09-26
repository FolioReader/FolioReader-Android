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
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.adapter.FolioPageFragmentAdapter;
import com.folioreader.adapter.TOCAdapter;
import com.folioreader.fragments.FolioPageFragment;
import com.folioreader.model.Highlight;
import com.folioreader.model.SmilElements;
import com.folioreader.smil.AudioElement;
import com.folioreader.smil.SmilFile;
import com.folioreader.smil.TextElement;
import com.folioreader.util.AppUtil;
import com.folioreader.util.EpubManipulator;
import com.folioreader.util.ProgressDialog;
import com.folioreader.view.AudioView;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;
import com.folioreader.view.VerticalViewPager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
        FolioViewCallback, FolioPageFragment.FolioPageFragmentCallback, TOCAdapter.ChapterSelectionCallBack {

    public static final String INTENT_EPUB_ASSET_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final int ACTION_HIGHLIGHT_lIST = 77;
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private static final String ITEM_DELETED = "item_deleted";
    public static enum Epub_Source_Type {
        RAW,
        ASSESTS,
        SD_CARD
    };

    private RecyclerView mRecyclerViewMenu;
    private VerticalViewPager mFolioPageViewPager;
    private FolioView mFolioView;
    private ConfigView mConfigView;
    private AudioView mAudioView;
    private Toolbar mToolbar;
    private TOCAdapter mTocAdapter;

    private String mEpubAssetPath;
    private String mSourceType;
    private int mRawId;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences;
    private List<SpineReference> mSpineReferences;
    private List<AudioElement> mAudioElementArrayList;
    private List<TextElement> mTextElementList;

    public boolean mIsActionBarVisible;
    public  boolean mIsSmilParsed=false;
    private int mChapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
//        mSourceType=getIntent().getStringExtra(INTENT_EPUB_SOURCE_TYPE);
        initBook();
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

        findViewById(R.id.btn_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsSmilParsed) {
                    if (mAudioView.isDragViewAboveTheLimit()) {
                        mAudioView.moveToOriginalPosition();
                    } else {
                        mAudioView.moveOffScreen();
                    }
                } else {
                    Toast.makeText(FolioActivity.this,getString(R.string.please_wait_till_audio_is_parsed),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initBook() {
        final Dialog pgDailog = ProgressDialog.show(FolioActivity.this, getString(R.string.please_wait));
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream epubInputStream=null;

                   //String rawUrl="android.resource://" + getPackageName() + "/" + R.raw.a;
                    /*if(mSourceType.equals("raw")){
                        mRawId = getIntent().getIntExtra(INTENT_EPUB_ASSET_PATH,0);
                        Resources res = getResources();
                        epubInputStream = res.openRawResource(mRawId);
                    } else if(mSourceType.equals("assest")){
                        mEpubAssetPath = getIntent().getStringExtra(INTENT_EPUB_ASSET_PATH);
                        AssetManager assetManager = getAssets();
                        epubInputStream = assetManager.open(mEpubAssetPath);
                    }*/

                    mBook = AppUtil.saveEpubFile(getIntent().getExtras(), FolioActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadBook();
                            if (pgDailog != null && pgDailog.isShowing()) pgDailog.dismiss();
                        }
                    });
            }
        }).start();
    }

    private void loadBook() {
        configRecyclerViews();
        configFolio();
        parseSmil();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mRecyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
        mFolioView = (FolioView) findViewById(R.id.folio_view);
        mConfigView = (ConfigView) findViewById(R.id.config_view);
        mAudioView = (AudioView) findViewById(R.id.audio_view);
        mFolioView.setFolioViewCallback(FolioActivity.this);
        mConfigView.setConfigViewCallback(FolioActivity.this);
        mAudioView.setAudioViewCallback(FolioActivity.this);
        mConfigView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConfigView.moveOffScreen();
                mAudioView.moveOffScreen();
            }
        }, 2);
        configDrawerLayoutButtons();
    }

    @Override
    public void onBackPressed() {
        if (mConfigView.isDragViewAboveTheLimit()) {
            mConfigView.moveToOriginalPosition();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackgroundUpdate(int value) {
        mRecyclerViewMenu.setBackgroundColor(value);
        mFolioView.setBackgroundColor(value);

    }

    @Override
    public void changeMenuTextColor() {
        mTocAdapter.setNightMode(!Config.getConfig().isNightMode());
        mTocAdapter.setSelectedChapterPosition(mChapterPosition);
        mTocAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShadowAlpha(float alpha) {
        mFolioView.updateShadowAlpha(alpha);
    }

    @Override
    public void showShadow() {
        mFolioView.resetView();
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

    @Override
    public void onAudioPlayed() {
        //mFolioPageViewPager.setCurrentItem(mAudioPagePosition);
    }

    private Fragment getFragment(int pos) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.folioPageViewPager + ":" + (pos));
    }

    @Override
    public void onShadowClick() {
        mConfigView.moveOffScreen();
        mAudioView.moveOffScreen();
    }

    public void configRecyclerViews() {
        mTocReferences = (ArrayList<TOCReference>) mBook.getTableOfContents().getTocReferences();
        mSpineReferences = mBook.getSpine().getSpineReferences();
        setSpineReferenceTitle();
        mRecyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        if (mTocReferences != null) {
            mTocAdapter = new TOCAdapter(mTocReferences, FolioActivity.this);
            mRecyclerViewMenu.setAdapter(mTocAdapter);
        }
    }

    public boolean setPagerToPosition(int audioPosition){
        String src = mTextElementList.get(audioPosition).getSrc();
        String temp[] = src.split("#");
        String hRef = "text//" + temp[0];
        String currentHref = mSpineReferences.get(mFolioPageViewPager.getCurrentItem()).getResource().getHref();
        if (hRef.equalsIgnoreCase(currentHref)){
            return false;
        } else {
            setPagerToPosition("text//" + temp[0]);
            return true;
        }
    }

    @Override
    public void onPageLoaded() {
        //mAudioView.startMediaPlayer();
    }

    public void setPagerToPosition(String href) {
        for (int i = 0; i < mSpineReferences.size(); i++) {
            if (AppUtil.compareUrl(href, mSpineReferences.get(i).getResource().getHref())) {
                mFolioPageViewPager.setCurrentItem(i, true);
                toolbarAnimateHide();
                break;
            }
        }
    }

    private void configFolio() {
        mFolioPageViewPager = (VerticalViewPager) mFolioView.findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChapterPosition = position;
                ((TextView) findViewById(R.id.lbl_center)).setText(mSpineReferences.get(position).getResource().getTitle());
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
    }

    private void setSpineReferenceTitle() {
        for (int j = 0; j < mSpineReferences.size(); j++) {
            String href = mSpineReferences.get(j).getResource().getHref();
            for (int i = 0; i < mTocReferences.size(); i++) {
                if (mTocReferences.get(i).getResource().getHref().equalsIgnoreCase(href)) {
                    mSpineReferences.get(j).getResource().setTitle(mTocReferences.get(i).getTitle());
                    break;
                } else {
                    mSpineReferences.get(j).getResource().setTitle("");
                }
            }
        }
        ((TextView) findViewById(R.id.lbl_center)).setText(mSpineReferences.get(0).getResource().getTitle());
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
                if (mConfigView.isDragViewAboveTheLimit()) {
                    mConfigView.moveToOriginalPosition();
                } else {
                    mConfigView.moveOffScreen();
                }
            }
        });

        findViewById(R.id.btn_highlight_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerLayout) findViewById(R.id.drawer_left)).closeDrawer(findViewById(R.id.drawer_menu));
                Intent intent = new Intent(FolioActivity.this, HighlightListActivity.class);
                startActivityForResult(intent, ACTION_HIGHLIGHT_lIST);
            }
        });
    }

    @Override
    public String getChapterHtmlContent(int position) {
        return readHTmlString(position);
    }

    @Override
    public void hideOrshowToolBar() {
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

    private String readHTmlString(int position) {
        String pageHref = mSpineReferences.get(position).getResource().getHref();
        pageHref = Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/" + pageHref;
        String html = EpubManipulator.readPage(pageHref);
        return html;
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
        }, 10000);

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

    public Highlight setCurrentPagerPostion(Highlight highlight) {
        highlight.setCurrentPagerPostion(mFolioPageViewPager.getCurrentItem());
        return highlight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_HIGHLIGHT_lIST && resultCode == RESULT_OK) {
            if (data.hasExtra(HIGHLIGHT_ITEM)) {
                Highlight highlight = data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position = highlight.getCurrentPagerPostion();
                mFolioPageViewPager.setCurrentItem(position);
                Fragment fragment = getFragment(position);
                ((FolioPageFragment) fragment).setWebViewPosition(highlight.getCurrentWebviewScrollPos());
            } else if (data.hasExtra(ITEM_DELETED)) {
                ((FolioPageFragment) getFragment(mChapterPosition)).reload();

            }
        }
    }

    private void parseSmil() {
        mIsSmilParsed=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SmilElements smilElements = AppUtil.retrieveAndParseSmilJSON(FolioActivity.this);
                if (smilElements != null) {
                    mTextElementList = smilElements.getTextElementArrayList();
                    mAudioElementArrayList = smilElements.getAudioElementArrayList();
                } else {
                    SmilFile smilFile = AppUtil.createSmilJson(FolioActivity.this);
                    if(smilFile!=null) {
                        mAudioElementArrayList = smilFile.getAudioSegments();
                        mTextElementList = smilFile.getTextSegments();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIsSmilParsed=true;
                    }
                });
            }
        }).start();

    }


    public void setHighLight(int position, String style) {
        String src = mTextElementList.get(position).getSrc();
        String temp[] = src.split("#");
        String textId = temp[1];
        //setPagerToPosition("text//" + temp[0]);
        ((FolioPageFragment) getFragment(mChapterPosition)).highLightString(textId, style);
    }


    public void setHighLightStyle(String style) {
        ((FolioPageFragment) getFragment(mChapterPosition)).setStyle(style);
    }

    public AudioElement getElement(int position) {
        return mAudioElementArrayList.get(position);
    }


   /* @Override
    protected void onResume() {
        super.onResume();
        if (mAudioView != null) {
            mAudioView.playerResume();
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioView != null) {
            mAudioView.playerStop();
        }
    }
}
