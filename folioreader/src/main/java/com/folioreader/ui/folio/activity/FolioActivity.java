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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.event.AnchorIdEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.WebViewPosition;
import com.folioreader.ui.folio.adapter.FolioPageFragmentAdapter;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.ui.folio.presenter.MainMvpView;
import com.folioreader.ui.folio.presenter.MainPresenter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.view.ConfigBottomSheetDialogFragment;
import com.folioreader.view.DirectionalViewpager;
import com.folioreader.view.FolioToolbar;
import com.folioreader.view.FolioToolbarCallback;
import com.folioreader.view.MediaControllerCallback;
import com.folioreader.view.MediaControllerView;
import com.folioreader.view.ObservableWebView;

import org.greenrobot.eventbus.EventBus;
import org.readium.r2_streamer.model.container.Container;
import org.readium.r2_streamer.model.container.EpubContainer;
import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.publication.link.Link;
import org.readium.r2_streamer.server.EpubServer;
import org.readium.r2_streamer.server.EpubServerSingleton;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.HIGHLIGHT_SELECTED;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;

public class FolioActivity
        extends AppCompatActivity
        implements FolioPageFragment.FolioPageFragmentCallback,
        ObservableWebView.ToolBarListener,
        ConfigBottomSheetDialogFragment.ConfigDialogCallback,
        MainMvpView,
        MediaControllerCallback,
        FolioToolbarCallback {

    private static final String TAG = "FolioActivity";

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final String INTENT_HIGHLIGHTS_LIST = "highlight_list";
    public static final String EXTRA_READ_POSITION = "com.folioreader.extra.READ_POSITION";

    public enum EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD
    }

    public static final int ACTION_CONTENT_HIGHLIGHT = 77;
    private String bookFileName;
    private static final String HIGHLIGHT_ITEM = "highlight_item";

    private DirectionalViewpager mFolioPageViewPager;
    private FolioToolbar toolbar;

    private int mChapterPosition;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private ReadPosition entryReadPosition;

    private List<Link> mSpineReferenceList = new ArrayList<>();
    private EpubServer mEpubServer;

    private Config mConfig;
    private String mBookId;
    private String mEpubFilePath;
    private EpubSourceType mEpubSourceType;
    int mEpubRawId = 0;
    private MediaControllerView mediaControllerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfig();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);

        mBookId = getIntent().getStringExtra(FolioReader.INTENT_BOOK_ID);
        mEpubSourceType = (EpubSourceType)
                getIntent().getExtras().getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        if (mEpubSourceType.equals(EpubSourceType.RAW)) {
            mEpubRawId = getIntent().getExtras().getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        } else {
            mEpubFilePath = getIntent().getExtras()
                    .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        }
        mediaControllerView = findViewById(R.id.media_controller_view);
        mediaControllerView.setListeners(this);

        if (ContextCompat.checkSelfPermission(FolioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FolioActivity.this, Constants.getWriteExternalStoragePerms(), Constants.WRITE_EXTERNAL_STORAGE_REQUEST);
        } else {
            if(setupBook() == false){
                Toast.makeText(this, R.string.cant_open_file, Toast.LENGTH_LONG);
                finish();
            }
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setListeners(this);
    }

    @Override
    public void showMediaController() {
        mediaControllerView.show();
    }

    @Override
    public void startContentHighlightActivity() {
        Intent intent = new Intent(FolioActivity.this, ContentHighlightActivity.class);
        intent.putExtra(CHAPTER_SELECTED, mSpineReferenceList.get(mChapterPosition).href);
        intent.putExtra(FolioReader.INTENT_BOOK_ID, mBookId);
        intent.putExtra(Constants.BOOK_TITLE, bookFileName);
        startActivityForResult(intent, ACTION_CONTENT_HIGHLIGHT);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    private Boolean initBook(String mEpubFileName, int mEpubRawId, String mEpubFilePath, EpubSourceType mEpubSourceType) {
        Log.v(TAG, "initBook >>");
        Boolean result = false;
        try {
            int portNumber = getIntent().getIntExtra(Config.INTENT_PORT, Constants.PORT_NUMBER);
            mEpubServer = EpubServerSingleton.getEpubServerInstance(portNumber);
            mEpubServer.start();
            String path = FileUtil.saveEpubFileAndLoadLazyBook(FolioActivity.this, mEpubSourceType, mEpubFilePath,
                    mEpubRawId, mEpubFileName);

            if(addEpub(path)){

                String urlString = Constants.LOCALHOST + URLEncoder.encode(bookFileName, "UTF-8") + "/manifest";
                Log.v(TAG, "initBook urlString = " + urlString);
                new MainPresenter(this).parseManifest(urlString);

                result = true;
            }
        } catch (IOException e) {
            Log.e(TAG, "initBook failed", e);
        }
        Log.v(TAG, "initBook <<");

        return result;
    }

    private Boolean addEpub(String path) throws IOException {
        Log.v(TAG, "addEpub >>");
        Log.v(TAG, "addEpub path = " + path);
        Log.v(TAG, "addEpub bookFileName = " + bookFileName);

        Boolean result = true;

        Container epubContainer = new EpubContainer(path);
        try{
            mEpubServer.addEpub(epubContainer, "/" + URLEncoder.encode(bookFileName));
        }
        catch(Exception ex){
            Log.e(TAG, "addEpub error = " +  ex.getMessage());
            result = false;
        }
        getEpubResource();

        Log.v(TAG, "addEpub <<");

        return result;
    }

    private void getEpubResource() {
    }

    @Override
    public void onOrientationChange(int orientation) {
        if (orientation == 0) {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.VERTICAL);
            mFolioPageFragmentAdapter =
                    new FolioPageFragmentAdapter(getSupportFragmentManager(),
                            mSpineReferenceList, bookFileName, mBookId);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setOffscreenPageLimit(1);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);

        } else {
            mFolioPageViewPager.setDirection(DirectionalViewpager.Direction.HORIZONTAL);
            mFolioPageFragmentAdapter =
                    new FolioPageFragmentAdapter(getSupportFragmentManager(),
                            mSpineReferenceList, bookFileName, mBookId);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
            mFolioPageViewPager.setCurrentItem(mChapterPosition);
        }
    }

    private void configFolio() {
        mFolioPageViewPager = findViewById(R.id.folioPageViewPager);
        mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, true));
                mediaControllerView.setPlayButtonDrawable();
                mChapterPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                    toolbar.setTitle(mSpineReferenceList.get(mChapterPosition).bookTitle);
                }
            }
        });

        if (mSpineReferenceList != null) {
            mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(), mSpineReferenceList, bookFileName, mBookId);
            mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);

            entryReadPosition = getIntent().getParcelableExtra(FolioActivity.EXTRA_READ_POSITION);
            mFolioPageViewPager.setCurrentItem(getChapterIndex(entryReadPosition));
        }
    }

    /**
     * Returns the index of the chapter by following priority -
     * 1. id
     * 2. href
     * 3. index
     *
     * @param readPosition Last read position
     * @return index of the chapter
     */
    private int getChapterIndex(ReadPosition readPosition) {
        if (readPosition == null) {
            return 0;

        } else if (!TextUtils.isEmpty(readPosition.getChapterId())) {
            return getChapterIndex("id", readPosition.getChapterId());

        } else if (!TextUtils.isEmpty(readPosition.getChapterHref())) {
            return getChapterIndex("href", readPosition.getChapterHref());

        } else if (readPosition.getChapterIndex() > -1
                && readPosition.getChapterIndex() < mSpineReferenceList.size()) {
            return readPosition.getChapterIndex();
        }

        return 0;
    }

    private int getChapterIndex(String caseString, String value) {
        for (int i = 0; i < mSpineReferenceList.size(); i++) {
            switch (caseString) {
                case "id":
                    if (mSpineReferenceList.get(i).getId().equals(value))
                        return i;
                case "href":
                    if (mSpineReferenceList.get(i).getOriginalHref().equals(value))
                        return i;
            }
        }
        return 0;
    }

    @Override
    public void showConfigBottomSheetDialogFragment() {
        new ConfigBottomSheetDialogFragment().show(getSupportFragmentManager(), ConfigBottomSheetDialogFragment.class.getSimpleName());
    }

    @Override
    public void hideOrShowToolBar() {
        toolbar.showOrHideIfVisible();
    }

    @Override
    public void setPagerToPosition(String href) {
    }

    @Override
    public ReadPosition getEntryReadPosition() {
        if (entryReadPosition != null) {
            ReadPosition tempReadPosition = entryReadPosition;
            entryReadPosition = null;
            return tempReadPosition;
        }
        return null;
    }

    @Override
    public void goToChapter(String href) {
        href = href.substring(href.indexOf(bookFileName + "/") + bookFileName.length() + 1);
        for (Link spine : mSpineReferenceList) {
            if (spine.href.contains(href)) {
                mChapterPosition = mSpineReferenceList.indexOf(spine);
                mFolioPageViewPager.setCurrentItem(mChapterPosition);
                toolbar.setTitle(spine.getChapterTitle());
                break;
            }
        }
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
                        toolbar.setTitle(data.getStringExtra(Constants.BOOK_TITLE));
                        EventBus.getDefault().post(new AnchorIdEvent(selectedChapterHref));
                        break;
                    }
                }
            } else if (type.equals(HIGHLIGHT_SELECTED)) {
                HighlightImpl highlightImpl = data.getParcelableExtra(HIGHLIGHT_ITEM);
                int position = highlightImpl.getPageNumber();
                mFolioPageViewPager.setCurrentItem(position);
                EventBus.getDefault().post(new WebViewPosition(mSpineReferenceList.get(mChapterPosition).href, highlightImpl.getRangy()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEpubServer != null) {
            mEpubServer.stop();
        }
    }

    @Override
    public int getChapterPosition() {
        return mChapterPosition;
    }

    @Override
    public void onLoadPublication(EpubPublication publication) {
        mSpineReferenceList.addAll(publication.spines);
        if (publication.metadata.title != null) {
            toolbar.setTitle(publication.metadata.title);
        }

        if (mBookId == null) {
            if (publication.metadata.identifier != null) {
                mBookId = publication.metadata.identifier;
            } else {
                if (publication.metadata.title != null) {
                    mBookId = String.valueOf(publication.metadata.title.hashCode());
                } else {
                    mBookId = String.valueOf(bookFileName.hashCode());
                }
            }
        }
        configFolio();
    }

    private void setConfig() {
        Log.v(TAG, "setConfig >>");

        Bundle args = getIntent().getExtras();
        Log.v(TAG, "setConfig args = " + args.toString());

        if (getIntent().getParcelableExtra(Config.INTENT_CONFIG) != null) {
            mConfig = getIntent().getParcelableExtra(Config.INTENT_CONFIG);
            AppUtil.saveConfig(this, mConfig);
            Log.v(TAG, "setConfig (2) isShowTts = " + mConfig.isShowTts());
        } else         if (AppUtil.getSavedConfig(this) != null) {
            mConfig = AppUtil.getSavedConfig(this);
        } else {
            mConfig = new Config.ConfigBuilder().build();
            AppUtil.saveConfig(this, mConfig);
        }
        Log.v(TAG, "setConfig <<");
    }

    @Override
    public void play() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, true, false));
    }

    @Override
    public void pause() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(mSpineReferenceList.get(mChapterPosition).href, false, false));
    }

    @Override
    public void onError() {
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(String.format("%02x", messageDigest[i] & 0xff));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Boolean setupBook() {
        //bookFileName = FileUtil.getEpubFilename(this, mEpubSourceType, mEpubFilePath, mEpubRawId);
        bookFileName = md5(mEpubFilePath);
        return initBook(bookFileName, mEpubRawId, mEpubFilePath, mEpubSourceType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.WRITE_EXTERNAL_STORAGE_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBook();
                } else {
                    Toast.makeText(this, getString(R.string.cannot_access_epub_message), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }
}