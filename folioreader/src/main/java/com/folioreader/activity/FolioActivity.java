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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.adapter.FolioPageFragmentAdapter;
import com.folioreader.fragments.FolioPageFragment;
import com.folioreader.model.Highlight;
import com.folioreader.model.SmilElements;
import com.folioreader.smil.AudioElement;
import com.folioreader.smil.SmilFile;
import com.folioreader.smil.TextElement;
import com.folioreader.sqlite.DbAdapter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.EpubManipulator;
import com.folioreader.util.FileUtil;
import com.folioreader.util.ProgressDialog;
import com.folioreader.view.AudioViewBottomSheetDailogFragment;
import com.folioreader.view.ConfigBottomSheetDialogFragment;
import com.folioreader.view.DirectionalViewpager;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;

import static com.folioreader.Constants.BOOK;
import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.HIGHLIGHT_SELECTED;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;

public class FolioActivity extends AppCompatActivity implements
        FolioPageFragment.FolioPageFragmentCallback, ConfigBottomSheetDialogFragment.ConfigDialogCallback {

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final int ACTION_CONTENT_HIGHLIGHT = 77;
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    public static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public enum EpubSourceType {
        RAW,
        ASSESTS,
        SD_CARD
    }

    private DirectionalViewpager mFolioPageViewPager;
    private Toolbar mToolbar;

    private EpubSourceType mEpubSourceType;
    private String mEpubFilePath;
    private String mEpubFileName;
    private int mEpubRawId;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences;
    private List<SpineReference> mSpineReferences;
    private List<AudioElement> mAudioElementArrayList;
    private List<TextElement> mTextElementList = new ArrayList<>();

    public boolean mIsActionBarVisible;
    public boolean mIsSmilParsed = false;
    private int mChapterPosition;
    private boolean mIsSmilAvailable;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private int mWebViewScrollPosition;
    private int mPageLoadCount = 0;
    private ConfigBottomSheetDialogFragment mConfigBottomSheetDialogFragment;
    private AudioViewBottomSheetDailogFragment mAudioBottomSheetDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
        mEpubSourceType = (EpubSourceType)
                getIntent().getExtras().getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        if (mEpubSourceType.equals(EpubSourceType.RAW)) {
            mEpubRawId = getIntent().getExtras().getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        } else {
            mEpubFilePath = getIntent().getExtras()
                    .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        }

        mEpubFileName = FileUtil.getEpubFilename(this, mEpubSourceType, mEpubFilePath, mEpubRawId);
        initBook();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        findViewById(R.id.btn_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSmilParsed) {
                    if (mAudioBottomSheetDialogFragment == null) {
                        mAudioBottomSheetDialogFragment = new AudioViewBottomSheetDailogFragment();
                    }
                    mAudioBottomSheetDialogFragment.show(getSupportFragmentManager(), mAudioBottomSheetDialogFragment.getTag());

                } else {
                    Toast.makeText(FolioActivity.this,
                            getString(R.string.please_wait_till_audio_is_parsed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FolioActivity.this, ContentHighlightActivity.class);
                mBook.setResources(null);
                mBook.setNcxResource(null);
                intent.putExtra(BOOK, mBook);
                intent.putExtra(SELECTED_CHAPTER_POSITION, mChapterPosition);
                startActivityForResult(intent, ACTION_CONTENT_HIGHLIGHT);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        BUS.register(this);
    }

    private void initBook() {
        final Dialog pgDailog = ProgressDialog.show(FolioActivity.this,
                getString(R.string.please_wait));
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBook = FileUtil.saveEpubFile(FolioActivity.this, mEpubSourceType, mEpubFilePath,
                        mEpubRawId, mEpubFileName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadBook();
                        if (pgDailog != null && pgDailog.isShowing()) pgDailog.dismiss();
                    }
                });
            }
        }).start();

        new DbAdapter(FolioActivity.this);
    }

    private void loadBook() {
        configRecyclerViews();
        configFolio();
        parseSmil();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        configDrawerLayoutButtons();
    }

    @Override
    public void onBackPressed() {
        saveBookState();
        super.onBackPressed();
    }


    @Override
    public void onConfigChange() {

        int position = mFolioPageViewPager.getCurrentItem();
        //reload previous, current and next fragment
       /* Fragment page;
        if (position != 0) {
            page = getFragment(position - 1);
            ((FolioPageFragment) page).reload();
        }
        page = getFragment(position);
        ((FolioPageFragment) page).reload();
        if (position < mSpineReferences.size()) {
            page = getFragment(position + 1);
            if (page != null) {
                ((FolioPageFragment) page).reload();
            }

        }*/
    }

    @Override
    public void onAudioPlayed() {
        //mFolioPageViewPager.setCurrentItem(mAudioPagePosition);
    }

    @Override
    public void onOrentationChange(int orentation) {
        if (orentation == 0) {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.VERTICAL);
            if (mBook != null && mSpineReferences != null) {
                mFolioPageFragmentAdapter =
                        new FolioPageFragmentAdapter(getSupportFragmentManager(),
                                mSpineReferences, mBook, mEpubFileName, mIsSmilParsed);
                mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
                mFolioPageViewPager.setOffscreenPageLimit(1);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
            }
        } else {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.HORIZONTAL);
            if (mBook != null && mSpineReferences != null) {
                mFolioPageFragmentAdapter =
                        new FolioPageFragmentAdapter(getSupportFragmentManager(),
                                mSpineReferences, mBook, mEpubFileName, mIsSmilParsed);
                mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
            }
        }
    }

    private Fragment getFragment(int pos) {
        return getSupportFragmentManager().
                findFragmentByTag("android:switcher:" + R.id.folioPageViewPager + ":" + (pos));
    }


    public void configRecyclerViews() {
        mTocReferences = (ArrayList<TOCReference>) mBook.getTableOfContents().getTocReferences();
        mSpineReferences = mBook.getSpine().getSpineReferences();
        setSpineReferenceTitle();
    }

    public boolean setPagerToPosition(int audioPosition) {
        String src = mTextElementList.get(audioPosition).getSrc();
        String[] temp = src.split("#");
        String href = "text//" + temp[0];
        String currentHref =
                mSpineReferences.get(mFolioPageViewPager.getCurrentItem())
                        .getResource().getHref();
        if (href.equalsIgnoreCase(currentHref)) {
            return false;
        } else {
            setPagerToPosition("text//" + temp[0]);
            return true;
        }
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
        mFolioPageViewPager = (DirectionalViewpager) findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChapterPosition = position;
                ((TextView) findViewById(R.id.lbl_center)).
                        setText(mSpineReferences.get(position).getResource().getTitle());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mBook != null && mSpineReferences != null) {
            mFolioPageFragmentAdapter =
                    new FolioPageFragmentAdapter(getSupportFragmentManager(),
                            mSpineReferences, mBook, mEpubFileName, mIsSmilParsed);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            if (AppUtil.checkPreviousBookStateExist(FolioActivity.this, mBook)) {
                mFolioPageViewPager.setCurrentItem(AppUtil.getPreviousBookStatePosition(FolioActivity.this, mBook));
            }
        }
    }

    private void setSpineReferenceTitle() {
        for (int j = 0; j < mSpineReferences.size(); j++) {
            String href = mSpineReferences.get(j).getResource().getHref();
            for (int i = 0; i < mTocReferences.size(); i++) {
                if (mTocReferences.get(i).getResource().getHref().equalsIgnoreCase(href)) {
                    mSpineReferences.get(j).getResource()
                            .setTitle(mTocReferences.get(i).getTitle());
                    break;
                } else {
                    mSpineReferences.get(j).getResource().setTitle("");
                }
            }
        }
        ((TextView) findViewById(R.id.lbl_center))
                .setText(mSpineReferences.get(0).getResource().getTitle());
    }

    private void configDrawerLayoutButtons() {
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBookState();
                finish();
            }
        });

        findViewById(R.id.btn_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfigBottomSheetDialogFragment = new ConfigBottomSheetDialogFragment();
                mConfigBottomSheetDialogFragment.show(getSupportFragmentManager(), mConfigBottomSheetDialogFragment.getTag());
            }
        });
    }

    private void saveBookState() {
        AppUtil.saveBookState(FolioActivity.this, mBook, mFolioPageViewPager.getCurrentItem(), mWebViewScrollPosition);
    }

    @Override
    public String getChapterHtmlContent(int position) {
        return readHTmlString(position);
    }

    @Override
    public void hideOrshowToolBar() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        } else {
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
        String opfpath = AppUtil.getPathOPF(FileUtil.getFolioEpubFolderPath(mEpubFileName), FolioActivity.this);
        if (AppUtil.checkOPFInRootDirectory(FileUtil.getFolioEpubFolderPath(mEpubFileName), FolioActivity.this)) {
            pageHref = FileUtil.getFolioEpubFolderPath(mEpubFileName) + "/" + pageHref;
        } else {
            pageHref = FileUtil.getFolioEpubFolderPath(mEpubFileName) + "/" + opfpath + "/" + pageHref;
        }
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

    public Highlight setCurrentPagerPostion(Highlight highlight) {
        highlight.setCurrentPagerPostion(mFolioPageViewPager.getCurrentItem());
        return highlight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_CONTENT_HIGHLIGHT && resultCode == RESULT_OK && data.hasExtra(TYPE)) {

            String type = data.getStringExtra(TYPE);
            if (type.equals(CHAPTER_SELECTED)) {
                mChapterPosition = data.getIntExtra(SELECTED_CHAPTER_POSITION, 0);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
            } else if (type.equals(HIGHLIGHT_SELECTED)) {
                Highlight highlight = data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position = highlight.getCurrentPagerPostion();
                mFolioPageViewPager.setCurrentItem(position);
                Fragment fragment = getFragment(position);
                ((FolioPageFragment) fragment).
                        setWebViewPosition(highlight.getCurrentWebviewScrollPos());
            }
        }
    }

    private void parseSmil() {
        mIsSmilParsed = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
               /* SmilElements smilElements = AppUtil.retrieveAndParseSmilJSON(FolioActivity.this,
                        mEpubFileName);
                if (smilElements != null) {
                   /* mTextElementList = smilElements.getTextElementArrayList();
                    mAudioElementArrayList = smilElements.getAudioElementArrayList();
                    mIsSmilAvailable = true;
                    FolioActivity.BUS.post(mTextElementList);
                } else {*/
                    SmilFile smilFile = AppUtil.createSmilJson(FolioActivity.this, mEpubFileName);
                    if (smilFile != null) {
                        mAudioElementArrayList = smilFile.getAudioSegments();
                        mTextElementList = smilFile.getTextSegments();
                        mIsSmilAvailable = true;
                        FolioActivity.BUS.post(mTextElementList);
                    } else {
                        mIsSmilAvailable = false;
                    }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIsSmilParsed = true;
                    }
                });
                }


        }).start();

    }

    public AudioElement getElement(int position) {
        if (mAudioElementArrayList != null) {
            return mAudioElementArrayList.get(position);
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (mAudioView != null) {
            mAudioView.playerStop();
        }*/
        if(mAudioBottomSheetDialogFragment!=null) {
            mAudioBottomSheetDialogFragment.unRegisterBus();
        }
    }

    @Override
    public void setLastWebViewPosition(int position) {
        mWebViewScrollPosition = position;
    }

    public String getEpubFileName() {
        return mEpubFileName;
    }

   /* public void getSentance() {
        ((FolioPageFragment) getFragment(mChapterPosition)).getTextSentence();
    }*/


 /*   public void speakSentence(Sentence sentance) {
        //mAudioView.speakAudio(sentance);
    }*/

    public void resetCurrentIndex() {
       // ((FolioPageFragment) getFragment(mChapterPosition)).resetCurrentIndex();
    }

    public boolean isSmilAvailable() {
        return mIsSmilAvailable;
    }

    public int getmChapterPosition() {
        return mChapterPosition;
    }


}
