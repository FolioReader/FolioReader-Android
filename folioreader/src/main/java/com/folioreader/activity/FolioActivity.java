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
import com.folioreader.util.SharedPreferenceUtil;
import com.folioreader.view.AudioView;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;
import com.folioreader.view.VerticalViewPager;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
        FolioViewCallback, FolioPageFragment.FolioPageFragmentCallback, TOCAdapter.ChapterSelectionCallBack {

    public static final String INTENT_EPUB_ASSET_PATH = "com.folioreader.epub_asset_path";
    public static final int ACTION_HIGHLIGHT_lIST = 77;
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private static final String ITEM_DELETED = "item_deleted";
    private static final String SMIL_ELEMENTS = "smil_elements";

    private RecyclerView recyclerViewMenu;
    private VerticalViewPager mFolioPageViewPager;
    private FolioView folioView;
    private ConfigView configView;
    private AudioView audioView;
    private Toolbar mToolbar;
    private List<AudioElement> mAudioElementArrayList;

    private String mEpubAssetPath;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences = new ArrayList<>();
    private List<SpineReference> mSpineReferences;
    private List<String> mSpineReferenceHtmls = new ArrayList<>();
    private boolean mIsActionBarVisible = false;
    private TOCAdapter mTocAdapter;
    private int mChapterPosition;
    private Dialog mProgressDailog;
    private List<TextElement> mTextElementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
        mProgressDailog = ProgressDialog.show(FolioActivity.this, getString(R.string.please_wait));
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

        findViewById(R.id.btn_speaker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioView.isDragViewAboveTheLimit()) {
                    audioView.moveToOriginalPosition();
                } else {
                    audioView.moveOffScreen();
                }
            }
        });
    }

    private void loadBook() {
        AssetManager assetManager = getAssets();
        try {
            final InputStream epubInputStream = assetManager.open(mEpubAssetPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppUtil.saveEpubFile(epubInputStream, FolioActivity.this);
                }
            }).start();
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
        audioView = (AudioView) findViewById(R.id.audio_view);
        folioView.setFolioViewCallback(FolioActivity.this);
        configView.setConfigViewCallback(FolioActivity.this);
        audioView.setAudioViewCallback(FolioActivity.this);
        configView.postDelayed(new Runnable() {
            @Override
            public void run() {
                configView.moveOffScreen();
                audioView.moveOffScreen();
            }
        }, 2);
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

    @Override
    public void onAudioPlayed() {
        //mFolioPageViewPager.setCurrentItem(mAudioPagePosition);
    }

    private Fragment getFragment(int pos) {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.folioPageViewPager + ":" + (pos));
    }

    @Override
    public void onShadowClick() {
        configView.moveOffScreen();
        audioView.moveOffScreen();
    }

    public void configRecyclerViews(ArrayList<TOCReference> tocReferences, Book book, ArrayList<SpineReference> spineReferenceArrayList) {
        mTocReferences = tocReferences;
        mSpineReferences = spineReferenceArrayList;
        findTitle(0);
        String title = mSpineReferences.get(0).getResource().getTitle();
        mBook = book;
        mBook.setCoverImage(null);
        mBook.setResources(null);
        configFolio();
        if (mProgressDailog != null && mProgressDailog.isShowing()) {
            mProgressDailog.dismiss();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                parseSmil();
            }
        }).start();

        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        if (mTocReferences != null) {
            String hrf = mTocReferences.get(0).getCompleteHref();
            mTocAdapter = new TOCAdapter(mTocReferences, FolioActivity.this);
            recyclerViewMenu.setAdapter(mTocAdapter);
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
        mFolioPageViewPager = (VerticalViewPager) folioView.findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mChapterPosition = position;
                findTitle(position);
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

    private void findTitle(int position) {
        String href = mSpineReferences.get(position).getResource().getHref();
        for (int i = 0; i < mTocReferences.size(); i++) {
            if (mTocReferences.get(i).getResource().getHref().equalsIgnoreCase(href)) {
                ((TextView) findViewById(R.id.lbl_center)).setText(mTocReferences.get(i).getTitle());
                break;
            } else {
                ((TextView) findViewById(R.id.lbl_center)).setText("");
            }
        }
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

    @Override
    public void invalidateActionMode() {
    }


    private String readHTmlString(int position) {
        String pageHref = mSpineReferences.get(position).getResource().getHref();
        pageHref = Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/" + pageHref;
        String html = EpubManipulator.readPage(pageHref);
        return html;
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

    private String getSelectedTextFromPage() {
        int currentPosition = mFolioPageViewPager.getCurrentItem();
        FolioPageFragment folioPageFragment = (FolioPageFragment) getFragment(currentPosition);
        String selectedText = folioPageFragment.getSelectedText();
        return selectedText;
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String smilElmentsJson = SharedPreferenceUtil.getSharedPreferencesString(FolioActivity.this, SMIL_ELEMENTS, null);
        if (smilElmentsJson != null) {
            try {
                SmilElements smilElements = objectMapper.readValue(smilElmentsJson, SmilElements.class);
                mTextElementList = smilElements.getTextElementArrayList();
                mAudioElementArrayList = smilElements.getAudioElementArrayList();
                if (smilElements != null) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File f = null;
            File[] paths;

            try {
                // create new file
                f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/folioreader/temp/OEBPS/text");

                // create new filename filter
                FilenameFilter fileNameFilter = new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        if (name.lastIndexOf('.') > 0) {
                            // get last index for '.' char
                            int lastIndex = name.lastIndexOf('.');

                            // get extension
                            String str = name.substring(lastIndex);

                            // match path name extension
                            if (str.equals(".smil")) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
                // returns pathnames for files and directory
                paths = f.listFiles(fileNameFilter);
                SmilFile smilFile = new SmilFile();
                smilFile.load(paths[0].getPath());
                mAudioElementArrayList = smilFile.getAudioSegments();
                mTextElementList = smilFile.getTextSegments();
                SmilElements smilElement = new SmilElements(mAudioElementArrayList, mTextElementList);
                String smilElemets = objectMapper.writeValueAsString(smilElement);
                SharedPreferenceUtil.putSharedPreferencesString(FolioActivity.this, SMIL_ELEMENTS, smilElemets);
            } catch (Exception e) {
                // if any error occurs
                e.printStackTrace();
            }
        }
    }


    public void setHighLight(int position, String style) {

        String src = mTextElementList.get(position).getSrc();

        String temp[] = src.split("#");
        String textid = temp[1];
        if (position == 0) {
            setPagerToPosition("text//" + temp[0]);
        }
        ((FolioPageFragment) getFragment(mChapterPosition)).highLightString(textid, style);
    }


    public void setHighLightStyle(String style) {
        ((FolioPageFragment) getFragment(mChapterPosition)).setStyle(style);
    }

    public AudioElement getElement(int position) {
        return mAudioElementArrayList.get(position);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (audioView != null) {
            audioView.playerResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioView != null) {
            audioView.playerStop();
        }
    }
}
