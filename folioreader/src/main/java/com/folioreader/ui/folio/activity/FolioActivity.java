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
package com.folioreader.ui.folio.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.model.event.AnchorIdEvent;
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.MediaOverlaySpeedEvent;
import com.folioreader.model.event.WebViewPosition;
import com.folioreader.model.sqlite.DbAdapter;
import com.folioreader.ui.folio.adapter.FolioPageFragmentAdapter;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.ui.folio.presenter.MainMvpView;
import com.folioreader.ui.folio.presenter.MainPresenter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.util.ProgressDialog;
import com.folioreader.view.ConfigBottomSheetDialogFragment;
import com.folioreader.view.DirectionalViewpager;
import com.folioreader.view.StyleableTextView;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.readium.r2_streamer.model.container.Container;
import org.readium.r2_streamer.model.container.EpubContainer;
import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.publication.link.Link;
import org.readium.r2_streamer.server.EpubServer;
import org.readium.r2_streamer.server.EpubServerSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static com.folioreader.Constants.BOOK_TITLE;
import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.HIGHLIGHT_SELECTED;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;

public class FolioActivity
        extends AppCompatActivity
        implements FolioPageFragment.FolioPageFragmentCallback,
        ConfigBottomSheetDialogFragment.ConfigDialogCallback,
        MainMvpView {

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";

    public enum EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD
    }

    private boolean isOpen = true;

    public static final int ACTION_CONTENT_HIGHLIGHT = 77;
    public static String EPUB_TITLE = "";
    private static final String HIGHLIGHT_ITEM = "highlight_item";

    public static final Bus BUS = new Bus(ThreadEnforcer.MAIN);
    public boolean mIsActionBarVisible;
    private DirectionalViewpager mFolioPageViewPager;
    private Toolbar mToolbar;

    private int mChapterPosition;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private int mWebViewScrollPosition;
    private ConfigBottomSheetDialogFragment mConfigBottomSheetDialogFragment;
    private TextView title;

    private List<Link> mSpineReferenceList = new ArrayList<>();
    private EpubServer mEpubServer;

    private Animation slide_down;
    private Animation slide_up;
    private boolean mIsNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
        BUS.register(this);
        String mEpubFilePath = getIntent().getExtras()
                .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);

        title = (TextView) findViewById(R.id.lbl_center);
        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        int mEpubRawId = 0;
        EpubSourceType mEpubSourceType = (EpubSourceType)
                getIntent().getExtras().getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        if (mEpubSourceType.equals(EpubSourceType.RAW)) {
            mEpubRawId = getIntent().getExtras().getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        } else {
            mEpubFilePath = getIntent().getExtras()
                    .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        }

        EPUB_TITLE = FileUtil.getEpubFilename(this, mEpubSourceType, mEpubFilePath, mEpubRawId);

        initBook(EPUB_TITLE, mEpubRawId, mEpubFilePath, mEpubSourceType);
        initAudioView();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        findViewById(R.id.btn_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FolioActivity.this, ContentHighlightActivity.class).putExtra(CHAPTER_SELECTED, mSpineReferenceList.get(mChapterPosition).href);
                startActivityForResult(intent, ACTION_CONTENT_HIGHLIGHT);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        // speaker = (ImageView) findViewById(R.id.btn_speaker);
        findViewById(R.id.btn_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    audioContainer.startAnimation(slide_up);
                    audioContainer.setVisibility(View.VISIBLE);
                    shade.setVisibility(View.VISIBLE);
                } else {
                    audioContainer.startAnimation(slide_down);
                    audioContainer.setVisibility(View.INVISIBLE);
                    shade.setVisibility(View.GONE);
                }
                isOpen = !isOpen;
            }
        });

        mIsNightMode = Config.getConfig().isNightMode();
        if (mIsNightMode) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(FolioActivity.this, R.color.black));
            title.setTextColor(ContextCompat.getColor(FolioActivity.this, R.color.white));
            audioContainer.setBackgroundColor(ContextCompat.getColor(FolioActivity.this, R.color.night));
        }
    }

    private void initBook(String mEpubFileName, int mEpubRawId, String mEpubFilePath, EpubSourceType mEpubSourceType) {
        try {
            mEpubServer = EpubServerSingleton.getEpubServerInstance(Constants.PORT_NUMBER);
            mEpubServer.start();
            String path = FileUtil.saveEpubFileAndLoadLazyBook(FolioActivity.this, mEpubSourceType, mEpubFilePath,
                    mEpubRawId, mEpubFileName);
            addEpub(path);

            String urlString = Constants.LOCALHOST + EPUB_TITLE + "/manifest";
            MainPresenter mainPresenter = new MainPresenter(this);
            mainPresenter.parseManifest(urlString);

        } catch (IOException e) {
            e.printStackTrace();
        }

        new DbAdapter(FolioActivity.this);
    }

    private void addEpub(String path) throws IOException {
        Container epubContainer = new EpubContainer(path);
        mEpubServer.addEpub(epubContainer, "/" + EPUB_TITLE);
        getEpubResource();
    }

    private void getEpubResource() {
    }

    private void loadBook() {
        configFolio();
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
    public void onOrientationChange(int orentation) {
        if (orentation == 0) {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.VERTICAL);
            mFolioPageFragmentAdapter =
                    new FolioPageFragmentAdapter(getSupportFragmentManager(),
                            mSpineReferenceList, EPUB_TITLE);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setOffscreenPageLimit(1);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);

        } else {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.HORIZONTAL);
            mFolioPageFragmentAdapter =
                    new FolioPageFragmentAdapter(getSupportFragmentManager(),
                            mSpineReferenceList, EPUB_TITLE);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);
        }
    }

    private void configFolio() {
        mFolioPageViewPager = (DirectionalViewpager) findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                FolioActivity.BUS.post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, true));
                mPlayPauseBtn.setImageDrawable(ContextCompat.getDrawable(FolioActivity.this, R.drawable.play_icon));
                mChapterPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                    title.setText(mSpineReferenceList.get(mChapterPosition).bookTitle);
                }
            }
        });

        if (mSpineReferenceList != null) {
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(), mSpineReferenceList, EPUB_TITLE);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
        }

        if (AppUtil.checkPreviousBookStateExist(FolioActivity.this, EPUB_TITLE)) {
            mFolioPageViewPager.setCurrentItem(AppUtil.getPreviousBookStatePosition(FolioActivity.this, EPUB_TITLE));
        }
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
        if (mSpineReferenceList.size() > 0) {
            AppUtil.saveBookState(FolioActivity.this, EPUB_TITLE, mFolioPageViewPager.getCurrentItem(), mWebViewScrollPosition);
        }
    }

    @Override
    public void hideOrshowToolBar() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        } else {
            toolbarAnimateShow();
        }
    }

    @Override
    public void hideToolBarIfVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide();
        }
    }

    @Override
    public void setPagerToPosition(String href) {
    }

    @Override
    public void setLastWebViewPosition(int position) {
        this.mWebViewScrollPosition = position;
    }

    @Override
    public void goToChapter(String href) {
        href = href.substring(href.indexOf(EPUB_TITLE + "/") + EPUB_TITLE.length() + 1);
        for (Link spine : mSpineReferenceList) {
            if (spine.href.contains(href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                title.setText(spine.getChapterTitle());
                break;
            }
        }
    }

    private void toolbarAnimateShow() {
        if (!mIsActionBarVisible) {
            mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            mIsActionBarVisible = true;
        }
    }

    private void toolbarAnimateHide() {
        mIsActionBarVisible = false;
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
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
                String selectedChapterHref = data.getStringExtra(SELECTED_CHAPTER_POSITION);
                for (Link spine : mSpineReferenceList) {
                    if (selectedChapterHref.contains(spine.href)) {
                        mChapterPosition = mSpineReferenceList.indexOf(spine);
                        mFolioPageViewPager.setCurrentItem(mChapterPosition);
                        title.setText(data.getStringExtra(BOOK_TITLE));
                        BUS.post(new AnchorIdEvent(selectedChapterHref));
                        break;
                    }
                }
            } else if (type.equals(HIGHLIGHT_SELECTED)) {
                Highlight highlight = data.getParcelableExtra(HIGHLIGHT_ITEM);
                mWebViewScrollPosition = highlight.getCurrentWebviewScrollPos();
                int position = highlight.getCurrentPagerPostion();
                mFolioPageViewPager.setCurrentItem(position);
                BUS.post(new WebViewPosition(mWebViewScrollPosition, mSpineReferenceList.get(mChapterPosition).href));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEpubServer != null) {
            mEpubServer.stop();
        }
        BUS.unregister(this);
    }

    public int getmChapterPosition() {
        return mChapterPosition;
    }

    @Override
    public void onLoadPublication(EpubPublication publication) {
        mSpineReferenceList.addAll(publication.spines);
        if (publication.metadata.title != null) {
            title.setText(publication.metadata.title);
        }

        final Dialog pgDailog = ProgressDialog.show(FolioActivity.this,
                getString(R.string.please_wait));
        new Thread(new Runnable() {
            @Override
            public void run() {
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

    //*************************************************************************//
    //                           AUDIO PLAYER                                  //
    //*************************************************************************//
    private StyleableTextView mHalfSpeed, mOneSpeed, mTwoSpeed, mOneAndHalfSpeed;
    private StyleableTextView mBackgroundColorStyle, mUnderlineStyle, mTextColorStyle;
    private RelativeLayout audioContainer;
    private boolean mIsSpeaking;
    private ImageButton mPlayPauseBtn;
    private RelativeLayout shade;

    private void initAudioView() {
        mHalfSpeed = (StyleableTextView) findViewById(R.id.btn_half_speed);
        mOneSpeed = (StyleableTextView) findViewById(R.id.btn_one_x_speed);
        mTwoSpeed = (StyleableTextView) findViewById(R.id.btn_twox_speed);
        audioContainer = (RelativeLayout) findViewById(R.id.container);
        shade = (RelativeLayout) findViewById(R.id.shade);
        shade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    audioContainer.startAnimation(slide_up);
                    audioContainer.setVisibility(View.VISIBLE);
                    shade.setVisibility(View.VISIBLE);
                } else {
                    audioContainer.startAnimation(slide_down);
                    audioContainer.setVisibility(View.INVISIBLE);
                    shade.setVisibility(View.GONE);
                }
                isOpen = !isOpen;
            }
        });
        mOneAndHalfSpeed = (StyleableTextView) findViewById(R.id.btn_one_and_half_speed);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.play_button);
        mBackgroundColorStyle = (StyleableTextView) findViewById(R.id.btn_backcolor_style);
        mUnderlineStyle = (StyleableTextView) findViewById(R.id.btn_text_undeline_style);
        mTextColorStyle = (StyleableTextView) findViewById(R.id.btn_text_color_style);
        mIsSpeaking = false;

        Context mContext = mHalfSpeed.getContext();
        mOneAndHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.one_and_half_speed)));
        mHalfSpeed.setText(Html.fromHtml(mContext.getString(R.string.half_speed_text)));
        String styleUnderline =
                mHalfSpeed.getContext().getResources().getString(R.string.style_underline);
        mUnderlineStyle.setText(Html.fromHtml(styleUnderline));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            findViewById(R.id.playback_speed_Layout).setVisibility(GONE);
        }

        mPlayPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSpeaking) {
                    FolioActivity.BUS.post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, false));
                    mPlayPauseBtn.setImageDrawable(ContextCompat.getDrawable(FolioActivity.this, R.drawable.play_icon));
                } else {
                    FolioActivity.BUS.post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, true, false));
                    mPlayPauseBtn.setImageDrawable(ContextCompat.getDrawable(FolioActivity.this, R.drawable.pause_btn));
                }
                mIsSpeaking = !mIsSpeaking;
            }
        });

        mHalfSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(true);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);
                FolioActivity.BUS.post(new MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.HALF));
            }
        });

        mOneSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(true);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(false);
                FolioActivity.BUS.post(new MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE));
            }
        });
        mOneAndHalfSpeed.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(true);
                mTwoSpeed.setSelected(false);
                FolioActivity.BUS.post(new MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.ONE_HALF));
            }
        });
        mTwoSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHalfSpeed.setSelected(false);
                mOneSpeed.setSelected(false);
                mOneAndHalfSpeed.setSelected(false);
                mTwoSpeed.setSelected(true);
                FolioActivity.BUS.post(new MediaOverlaySpeedEvent(MediaOverlaySpeedEvent.Speed.TWO));
            }
        });

        mBackgroundColorStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(true);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(false);
                FolioActivity.BUS.post(new MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.DEFAULT));
            }
        });

        mUnderlineStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(true);
                mTextColorStyle.setSelected(false);
                FolioActivity.BUS.post(new MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.UNDERLINE));

            }
        });

        mTextColorStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackgroundColorStyle.setSelected(false);
                mUnderlineStyle.setSelected(false);
                mTextColorStyle.setSelected(true);
                FolioActivity.BUS.post(new MediaOverlayHighlightStyleEvent(MediaOverlayHighlightStyleEvent.Style.BACKGROUND));
            }
        });
    }

    @Override
    public void onError() {
    }
}
