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
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.ReadPosition;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.search.SearchItem;
import com.folioreader.ui.folio.adapter.FolioPageFragmentAdapter;
import com.folioreader.ui.folio.adapter.SearchAdapter;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.ui.folio.fragment.MediaControllerFragment;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.util.UiUtil;
import com.folioreader.view.ConfigBottomSheetDialogFragment;
import com.folioreader.view.DirectionalViewpager;
import com.folioreader.view.FolioAppBarLayout;
import com.folioreader.view.MediaControllerCallback;

import org.greenrobot.eventbus.EventBus;
import org.readium.r2.shared.Link;
import org.readium.r2.shared.Publication;
import org.readium.r2.streamer.parser.CbzParser;
import org.readium.r2.streamer.parser.EpubParser;
import org.readium.r2.streamer.parser.PubBox;
import org.readium.r2.streamer.server.Server;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.folioreader.Constants.CHAPTER_SELECTED;
import static com.folioreader.Constants.HIGHLIGHT_SELECTED;
import static com.folioreader.Constants.SELECTED_CHAPTER_POSITION;
import static com.folioreader.Constants.TYPE;

public class FolioActivity
        extends AppCompatActivity
        implements FolioActivityCallback, MediaControllerCallback,
        View.OnSystemUiVisibilityChangeListener {

    private static final String LOG_TAG = "FolioActivity";

    public static final String INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path";
    public static final String INTENT_EPUB_SOURCE_TYPE = "epub_source_type";
    public static final String EXTRA_READ_POSITION = "com.folioreader.extra.READ_POSITION";
    private static final String BUNDLE_READ_POSITION_CONFIG_CHANGE = "BUNDLE_READ_POSITION_CONFIG_CHANGE";
    private static final String BUNDLE_DISTRACTION_FREE_MODE = "BUNDLE_DISTRACTION_FREE_MODE";
    public static final String EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM";
    public static final String ACTION_SEARCH_CLEAR = "ACTION_SEARCH_CLEAR";

    public enum EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD
    }

    private String bookFileName;
    private static final String HIGHLIGHT_ITEM = "highlight_item";

    private DirectionalViewpager mFolioPageViewPager;
    private ActionBar actionBar;
    private FolioAppBarLayout appBarLayout;
    private Toolbar toolbar;
    private boolean distractionFreeMode;
    private Handler handler;

    private int currentChapterIndex;
    private FolioPageFragmentAdapter mFolioPageFragmentAdapter;
    private ReadPosition entryReadPosition;
    private ReadPosition lastReadPosition;
    private Bundle outState;
    private Bundle savedInstanceState;

    private Server r2StreamerServer;
    private PubBox pubBox;
    private List<Link> spine;

    private String mBookId;
    private String mEpubFilePath;
    private EpubSourceType mEpubSourceType;
    int mEpubRawId = 0;
    private MediaControllerFragment mediaControllerFragment;
    private Config.Direction direction = Config.Direction.VERTICAL;
    private Uri searchUri;
    private Bundle searchAdapterDataBundle;
    private CharSequence searchQuery;
    private SearchItem searchItem;
    private DisplayMetrics displayMetrics;
    private float density;
    private Boolean topActivity;
    private int taskImportance;

    private enum RequestCode {
        CONTENT_HIGHLIGHT(77),
        SEARCH(101);

        final int value;

        RequestCode(int value) {
            this.value = value;
        }
    }

    private BroadcastReceiver closeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "-> closeBroadcastReceiver -> onReceive -> " + intent.getAction());

            String action = intent.getAction();
            if (action != null && action.equals(FolioReader.ACTION_CLOSE_FOLIOREADER)) {

                try {
                    ActivityManager activityManager = (ActivityManager)
                            context.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningAppProcessInfo> tasks =
                            activityManager.getRunningAppProcesses();
                    taskImportance = tasks.get(0).importance;
                } catch (Exception e) {
                    Log.e(LOG_TAG, "-> ", e);
                }

                Intent closeIntent = new Intent(getApplicationContext(), FolioActivity.class);
                closeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                closeIntent.setAction(FolioReader.ACTION_CLOSE_FOLIOREADER);
                FolioActivity.this.startActivity(closeIntent);
            }
        }
    };

    @SuppressWarnings("PMD.CollapsibleIfStatements")
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.v(LOG_TAG, "-> onNewIntent");

        String action = getIntent().getAction();
        if (action != null && action.equals(FolioReader.ACTION_CLOSE_FOLIOREADER)) {

            if (topActivity == null || !topActivity) {
                // FolioActivity was already left, so no need to broadcast ReadPosition again.
                // Finish activity without going through onPause() and onStop()
                finish();

                // To determine if app in background or foreground
                boolean appInBackground = false;
                if (Build.VERSION.SDK_INT < 26) {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND == taskImportance)
                        appInBackground = true;
                } else {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED == taskImportance)
                        appInBackground = true;
                }
                if (appInBackground)
                    moveTaskToBack(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "-> onResume");
        topActivity = true;

        String action = getIntent().getAction();
        if (action != null && action.equals(FolioReader.ACTION_CLOSE_FOLIOREADER)) {
            // FolioActivity is topActivity, so need to broadcast ReadPosition.
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "-> onStop");
        topActivity = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Need to add when vector drawables support library is used.
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        handler = new Handler();
        Display display = getWindowManager().getDefaultDisplay();
        displayMetrics = getResources().getDisplayMetrics();
        display.getRealMetrics(displayMetrics);
        density = displayMetrics.density;
        LocalBroadcastManager.getInstance(this).registerReceiver(closeBroadcastReceiver,
                new IntentFilter(FolioReader.ACTION_CLOSE_FOLIOREADER));

        // Fix for screen get turned off while reading
        // TODO -> Make this configurable
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setConfig(savedInstanceState);
        initDistractionFreeMode(savedInstanceState);

        setContentView(R.layout.folio_activity);
        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            searchAdapterDataBundle = savedInstanceState.getBundle(SearchAdapter.DATA_BUNDLE);
            searchQuery = savedInstanceState.getCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY);
        }

        mBookId = getIntent().getStringExtra(FolioReader.INTENT_BOOK_ID);
        mEpubSourceType = (EpubSourceType)
                getIntent().getExtras().getSerializable(FolioActivity.INTENT_EPUB_SOURCE_TYPE);
        if (mEpubSourceType.equals(EpubSourceType.RAW)) {
            mEpubRawId = getIntent().getExtras().getInt(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        } else {
            mEpubFilePath = getIntent().getExtras()
                    .getString(FolioActivity.INTENT_EPUB_SOURCE_PATH);
        }

        initActionBar();
        initMediaController();

        if (ContextCompat.checkSelfPermission(FolioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FolioActivity.this, Constants.getWriteExternalStoragePerms(), Constants.WRITE_EXTERNAL_STORAGE_REQUEST);
        } else {
            setupBook();
        }
    }

    private void initActionBar() {

        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        assert config != null;

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_drawer);
        UiUtil.setColorIntToDrawable(config.getThemeColor(), drawable);
        toolbar.setNavigationIcon(drawable);

        if (config.isNightMode()) {
            setNightMode();
        } else {
            setDayMode();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (config.isNightMode()) {
                color = ContextCompat.getColor(this, R.color.black);
            } else {
                int[] attrs = {android.R.attr.navigationBarColor};
                TypedArray typedArray = getTheme().obtainStyledAttributes(attrs);
                color = typedArray.getColor(0, ContextCompat.getColor(this, R.color.white));
            }
            getWindow().setNavigationBarColor(color);
        }

        if (Build.VERSION.SDK_INT < 16) {
            // Fix for appBarLayout.fitSystemWindows() not being called on API < 16
            appBarLayout.setTopMargin(getStatusBarHeight());
        }
    }

    @Override
    public void setDayMode() {
        Log.v(LOG_TAG, "-> setDayMode");

        actionBar.setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.white)));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black));
    }

    @Override
    public void setNightMode() {
        Log.v(LOG_TAG, "-> setNightMode");

        actionBar.setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(this, R.color.black)));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.night_title_text_color));
    }

    private void initMediaController() {
        Log.v(LOG_TAG, "-> initMediaController");

        mediaControllerFragment = MediaControllerFragment.
                getInstance(getSupportFragmentManager(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        assert config != null;
        UiUtil.setColorIntToDrawable(config.getThemeColor(), menu.findItem(R.id.itemSearch).getIcon());
        UiUtil.setColorIntToDrawable(config.getThemeColor(), menu.findItem(R.id.itemConfig).getIcon());
        UiUtil.setColorIntToDrawable(config.getThemeColor(), menu.findItem(R.id.itemTts).getIcon());

        if (!config.isShowTts())
            menu.findItem(R.id.itemTts).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.d(LOG_TAG, "-> onOptionsItemSelected -> " + item.getItemId());

        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            Log.v(LOG_TAG, "-> onOptionsItemSelected -> drawer");
            startContentHighlightActivity();
            return true;

        } else if (itemId == R.id.itemSearch) {
            Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.getTitle());
            if (searchUri == null)
                return true;
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.BUNDLE_SEARCH_URI, searchUri);
            intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle);
            intent.putExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery);
            startActivityForResult(intent, RequestCode.SEARCH.value);
            return true;

        } else if (itemId == R.id.itemConfig) {
            Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.getTitle());
            showConfigBottomSheetDialogFragment();
            return true;

        } else if (itemId == R.id.itemTts) {
            Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.getTitle());
            showMediaController();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startContentHighlightActivity() {

        Intent intent = new Intent(FolioActivity.this, ContentHighlightActivity.class);

        intent.putExtra(Constants.PUBLICATION, pubBox.getPublication());
        try {
            intent.putExtra(CHAPTER_SELECTED, spine.get(currentChapterIndex).getHref());
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            Log.w(LOG_TAG, "-> ", e);
            intent.putExtra(CHAPTER_SELECTED, "");
        }
        intent.putExtra(FolioReader.INTENT_BOOK_ID, mBookId);
        intent.putExtra(Constants.BOOK_TITLE, bookFileName);

        startActivityForResult(intent, RequestCode.CONTENT_HIGHLIGHT.value);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    public void showConfigBottomSheetDialogFragment() {
        new ConfigBottomSheetDialogFragment().show(getSupportFragmentManager(),
                ConfigBottomSheetDialogFragment.LOG_TAG);
    }

    public void showMediaController() {
        mediaControllerFragment.show(getSupportFragmentManager());
    }

    private void setupBook() {
        Log.v(LOG_TAG, "-> setupBook");
        try {
            initBook();
            onBookInitSuccess();
        } catch (Exception e) {
            Log.e(LOG_TAG, "-> Failed to initialize book", e);
            onBookInitFailure();
        }
    }

    private void initBook() throws Exception {
        Log.v(LOG_TAG, "-> initBook");

        bookFileName = FileUtil.getEpubFilename(this, mEpubSourceType, mEpubFilePath, mEpubRawId);
        String path = FileUtil.saveEpubFileAndLoadLazyBook(this, mEpubSourceType, mEpubFilePath,
                mEpubRawId, bookFileName);
        Publication.EXTENSION extension;
        String extensionString = null;
        try {
            extensionString = FileUtil.getExtensionUppercase(path);
            extension = Publication.EXTENSION.valueOf(extensionString);
        } catch (IllegalArgumentException e) {
            throw new Exception("-> Unknown book file extension `" + extensionString + "`", e);
        }

        switch (extension) {
            case EPUB:
                EpubParser epubParser = new EpubParser();
                pubBox = epubParser.parse(path, "");
                break;
            case CBZ:
                CbzParser cbzParser = new CbzParser();
                pubBox = cbzParser.parse(path, "");
                break;
        }

        int portNumber = getIntent().getIntExtra(Config.INTENT_PORT, Constants.PORT_NUMBER);
        r2StreamerServer = new Server(portNumber);
        r2StreamerServer.start();
        r2StreamerServer.addEpub(pubBox.getPublication(), pubBox.getContainer(),
                "/" + bookFileName, null);
    }

    public void onBookInitFailure() {
        //TODO -> Fail gracefully
    }

    public void onBookInitSuccess() {

        Publication publication = pubBox.getPublication();
        spine = publication.getSpine();
        setTitle(publication.getMetadata().getTitle());

        if (mBookId == null) {
            if (!publication.getMetadata().identifier.isEmpty()) {
                mBookId = publication.getMetadata().identifier;
            } else {
                if (!publication.getMetadata().getTitle().isEmpty()) {
                    mBookId = String.valueOf(publication.getMetadata().getTitle().hashCode());
                } else {
                    mBookId = String.valueOf(bookFileName.hashCode());
                }
            }
        }

        for (Link link : publication.getLinks()) {
            if (link.getRel().contains("search")) {
                searchUri = Uri.parse("http://" + link.getHref());
                break;
            }
        }
        if (searchUri == null)
            searchUri = Uri.parse(Constants.LOCALHOST + bookFileName + "/search");

        configFolio();
    }

    @Override
    public void onDirectionChange(@NonNull Config.Direction newDirection) {
        Log.v(LOG_TAG, "-> onDirectionChange");

        FolioPageFragment folioPageFragment = getCurrentFragment();
        if (folioPageFragment == null) return;
        entryReadPosition = folioPageFragment.getLastReadPosition();
        SearchItem searchItemVisible = folioPageFragment.searchItemVisible;

        direction = newDirection;

        mFolioPageViewPager.setDirection(newDirection);
        mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                spine, bookFileName, mBookId);
        mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);
        mFolioPageViewPager.setCurrentItem(currentChapterIndex);

        folioPageFragment = getCurrentFragment();
        if (folioPageFragment == null) return;
        if (searchItemVisible != null)
            folioPageFragment.highlightSearchItem(searchItemVisible);
    }

    public void initDistractionFreeMode(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "-> initDistractionFreeMode");

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        // Deliberately Hidden and shown to make activity contents lay out behind SystemUI
        hideSystemUI();
        showSystemUI();

        distractionFreeMode = savedInstanceState != null &&
                savedInstanceState.getBoolean(BUNDLE_DISTRACTION_FREE_MODE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.v(LOG_TAG, "-> onPostCreate");

        if (distractionFreeMode) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    }

    /**
     * @return returns height of status bar + app bar in dp.
     */
    @Override
    public int getTopDistraction() {
        int topDistraction = 0;
        if (!distractionFreeMode) {
            topDistraction = getStatusBarHeight();
            if (actionBar != null)
                topDistraction += actionBar.getHeight();
        }
        topDistraction /= density;
        Log.v(LOG_TAG, "-> getTopDistraction = " + topDistraction);
        return topDistraction;
    }

    /**
     * Calculates the bottom distraction which can cause due to navigation bar.
     * In mobile landscape mode, navigation bar is either to left or right of the screen.
     * In tablet, navigation bar is always at bottom of the screen.
     *
     * @return returns height of navigation bar in dp.
     */
    @Override
    public int getBottomDistraction() {
        int bottomDistraction = 0;
        if (!distractionFreeMode)
            bottomDistraction = appBarLayout.getNavigationBarHeight();
        bottomDistraction /= density;
        Log.v(LOG_TAG, "-> getBottomDistraction = " + bottomDistraction);
        return bottomDistraction;
    }

    /**
     * Calculates the Rect for visible viewport of the webview.
     * Visible viewport changes in following cases -
     * 1. In distraction free mode,
     * 2. In mobile landscape mode as navigation bar is placed either on left or right side,
     * 3. In tablets, navigation bar is always placed at bottom of the screen.
     */
    @Override
    public Rect getViewportRect() {
        //Log.v(LOG_TAG, "-> getViewportRect");

        Rect viewportRect = new Rect(appBarLayout.getInsets());
        if (distractionFreeMode)
            viewportRect.left = 0;
        viewportRect.top = (int) (getTopDistraction() * density);
        if (distractionFreeMode) {
            viewportRect.right = displayMetrics.widthPixels;
        } else {
            viewportRect.right = displayMetrics.widthPixels - viewportRect.right;
        }
        viewportRect.bottom = displayMetrics.heightPixels - ((int) (getBottomDistraction() * density));
        return viewportRect;
    }

    @Override
    public WeakReference<FolioActivity> getActivity() {
        return new WeakReference<>(this);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.v(LOG_TAG, "-> onSystemUiVisibilityChange -> visibility = " + visibility);

        distractionFreeMode = visibility != View.SYSTEM_UI_FLAG_VISIBLE;
        Log.v(LOG_TAG, "-> distractionFreeMode = " + distractionFreeMode);

        if (actionBar != null) {
            if (distractionFreeMode) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }
    }

    @Override
    public void toggleSystemUI() {

        if (distractionFreeMode) {
            showSystemUI();
        } else {
            hideSystemUI();
        }
    }

    public void showSystemUI() {
        Log.v(LOG_TAG, "-> showSystemUI");

        if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (appBarLayout != null)
                appBarLayout.setTopMargin(getStatusBarHeight());
            onSystemUiVisibilityChange(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public void hideSystemUI() {
        Log.v(LOG_TAG, "-> hideSystemUI");

        if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            // Specified 1 just to mock anything other than View.SYSTEM_UI_FLAG_VISIBLE
            onSystemUiVisibilityChange(1);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            result = getResources().getDimensionPixelSize(resourceId);
        return result;
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

    /**
     * Go to chapter specified by href
     *
     * @param href http link or relative link to the page or to the anchor
     * @return true if href is of EPUB or false if other link
     */
    @Override
    public boolean goToChapter(String href) {

        for (Link link : spine) {
            if (href.contains(link.getHref())) {
                currentChapterIndex = spine.indexOf(link);
                mFolioPageViewPager.setCurrentItem(currentChapterIndex);
                FolioPageFragment folioPageFragment = getCurrentFragment();
                folioPageFragment.scrollToFirst();
                folioPageFragment.scrollToAnchorId(href);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RequestCode.SEARCH.value) {
            Log.v(LOG_TAG, "-> onActivityResult -> " + RequestCode.SEARCH);

            if (resultCode == RESULT_CANCELED)
                return;

            searchAdapterDataBundle = data.getBundleExtra(SearchAdapter.DATA_BUNDLE);
            searchQuery = data.getCharSequenceExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY);

            if (resultCode == SearchActivity.ResultCode.ITEM_SELECTED.getValue()) {

                searchItem = data.getParcelableExtra(EXTRA_SEARCH_ITEM);
                // In case if SearchActivity is recreated due to screen rotation then FolioActivity
                // will also be recreated, so mFolioPageViewPager might be null.
                if (mFolioPageViewPager == null) return;
                currentChapterIndex = getChapterIndex(Constants.HREF, searchItem.getHref());
                mFolioPageViewPager.setCurrentItem(currentChapterIndex);
                FolioPageFragment folioPageFragment = getCurrentFragment();
                if (folioPageFragment == null) return;
                folioPageFragment.highlightSearchItem(searchItem);
                searchItem = null;
            }

        } else if (requestCode == RequestCode.CONTENT_HIGHLIGHT.value && resultCode == RESULT_OK &&
                data.hasExtra(TYPE)) {

            String type = data.getStringExtra(TYPE);

            if (type.equals(CHAPTER_SELECTED)) {
                goToChapter(data.getStringExtra(SELECTED_CHAPTER_POSITION));

            } else if (type.equals(HIGHLIGHT_SELECTED)) {
                HighlightImpl highlightImpl = data.getParcelableExtra(HIGHLIGHT_ITEM);
                currentChapterIndex = highlightImpl.getPageNumber();
                mFolioPageViewPager.setCurrentItem(currentChapterIndex);
                FolioPageFragment folioPageFragment = getCurrentFragment();
                if (folioPageFragment == null) return;
                folioPageFragment.scrollToHighlightId(highlightImpl.getRangy());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (outState != null)
            outState.putParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE, lastReadPosition);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(searchReceiver);
        localBroadcastManager.unregisterReceiver(closeBroadcastReceiver);

        if (r2StreamerServer != null) {
            r2StreamerServer.stop();
        }

        if (isFinishing())
            localBroadcastManager.sendBroadcast(new Intent(FolioReader.ACTION_FOLIOREADER_CLOSED));
    }

    @Override
    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    private void configFolio() {

        mFolioPageViewPager = findViewById(R.id.folioPageViewPager);
        // Replacing with addOnPageChangeListener(), onPageSelected() is not invoked
        mFolioPageViewPager.setOnPageChangeListener(new DirectionalViewpager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(LOG_TAG, "-> onPageSelected -> DirectionalViewpager -> position = " + position);

                EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(
                        spine.get(currentChapterIndex).getHref(), false, true));
                mediaControllerFragment.setPlayButtonDrawable();
                currentChapterIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                    int position = mFolioPageViewPager.getCurrentItem();
                    Log.v(LOG_TAG, "-> onPageScrollStateChanged -> DirectionalViewpager -> " +
                            "position = " + position);

                    FolioPageFragment folioPageFragment =
                            (FolioPageFragment) mFolioPageFragmentAdapter.getItem(position - 1);
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToLast();
                        if (folioPageFragment.mWebview != null)
                            folioPageFragment.mWebview.dismissPopupWindow();
                    }

                    folioPageFragment =
                            (FolioPageFragment) mFolioPageFragmentAdapter.getItem(position + 1);
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToFirst();
                        if (folioPageFragment.mWebview != null)
                            folioPageFragment.mWebview.dismissPopupWindow();
                    }
                }
            }
        });

        mFolioPageViewPager.setDirection(direction);
        mFolioPageFragmentAdapter = new FolioPageFragmentAdapter(getSupportFragmentManager(),
                spine, bookFileName, mBookId);
        mFolioPageViewPager.setAdapter(mFolioPageFragmentAdapter);

        // In case if SearchActivity is recreated due to screen rotation then FolioActivity
        // will also be recreated, so searchItem is checked here.
        if (searchItem != null) {

            currentChapterIndex = getChapterIndex(Constants.HREF, searchItem.getHref());
            mFolioPageViewPager.setCurrentItem(currentChapterIndex);
            FolioPageFragment folioPageFragment = getCurrentFragment();
            if (folioPageFragment == null) return;
            folioPageFragment.highlightSearchItem(searchItem);
            searchItem = null;

        } else {

            ReadPosition readPosition;
            if (savedInstanceState == null) {
                readPosition = getIntent().getParcelableExtra(FolioActivity.EXTRA_READ_POSITION);
                entryReadPosition = readPosition;
            } else {
                readPosition = savedInstanceState.getParcelable(BUNDLE_READ_POSITION_CONFIG_CHANGE);
                lastReadPosition = readPosition;
            }
            currentChapterIndex = getChapterIndex(readPosition);
            mFolioPageViewPager.setCurrentItem(currentChapterIndex);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(searchReceiver,
                new IntentFilter(ACTION_SEARCH_CLEAR));
    }

    private int getChapterIndex(ReadPosition readPosition) {

        if (readPosition == null) {
            return 0;
        } else if (!TextUtils.isEmpty(readPosition.getChapterHref())) {
            return getChapterIndex(Constants.HREF, readPosition.getChapterHref());
        }

        return 0;
    }

    private int getChapterIndex(String caseString, String value) {
        for (int i = 0; i < spine.size(); i++) {
            switch (caseString) {
                case Constants.HREF:
                    if (spine.get(i).getHref().equals(value))
                        return i;
            }
        }
        return 0;
    }

    /**
     * If called, this method will occur after onStop() for applications targeting platforms
     * starting with Build.VERSION_CODES.P. For applications targeting earlier platform versions
     * this method will occur before onStop() and there are no guarantees about whether it will
     * occur before or after onPause()
     *
     * @see Activity#onSaveInstanceState(Bundle) of Build.VERSION_CODES.P
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "-> onSaveInstanceState");
        this.outState = outState;

        outState.putBoolean(BUNDLE_DISTRACTION_FREE_MODE, distractionFreeMode);
        outState.putBundle(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle);
        outState.putCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery);
    }

    @Override
    public void storeLastReadPosition(ReadPosition lastReadPosition) {
        Log.v(LOG_TAG, "-> storeLastReadPosition");
        this.lastReadPosition = lastReadPosition;
    }

    private void setConfig(Bundle savedInstanceState) {

        Config config;
        Config intentConfig = getIntent().getParcelableExtra(Config.INTENT_CONFIG);
        boolean overrideConfig = getIntent().getBooleanExtra(Config.EXTRA_OVERRIDE_CONFIG, false);
        Config savedConfig = AppUtil.getSavedConfig(this);

        if (savedInstanceState != null) {
            config = savedConfig;

        } else if (savedConfig == null) {
            if (intentConfig == null) {
                config = new Config();
            } else {
                config = intentConfig;
            }

        } else {
            if (intentConfig != null && overrideConfig) {
                config = intentConfig;
            } else {
                config = savedConfig;
            }
        }

        // Code would never enter this if, just added for any unexpected error
        // and to avoid lint warning
        if (config == null)
            config = new Config();

        AppUtil.saveConfig(this, config);
        direction = config.getDirection();
    }

    @Override
    public void play() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(
                spine.get(currentChapterIndex).getHref(), true, false));
    }

    @Override
    public void pause() {
        EventBus.getDefault().post(new MediaOverlayPlayPauseEvent(
                spine.get(currentChapterIndex).getHref(), false, false));
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

    @Override
    public Config.Direction getDirection() {
        return direction;
    }

    private BroadcastReceiver searchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "-> searchReceiver -> onReceive -> " + intent.getAction());

            String action = intent.getAction();
            if (action == null)
                return;

            switch (action) {
                case ACTION_SEARCH_CLEAR:
                    //TODO -> rename reset to clear
                    resetSearchResults();
                    break;
            }
        }
    };

    private void resetSearchResults() {
        Log.v(LOG_TAG, "-> resetSearchResults");

        ArrayList<Fragment> fragments = mFolioPageFragmentAdapter.getFragments();
        for (int i = 0; i < fragments.size(); i++) {
            FolioPageFragment folioPageFragment = (FolioPageFragment) fragments.get(i);
            if (folioPageFragment != null) {
                folioPageFragment.resetSearchResults();
            }
        }

        ArrayList<Fragment.SavedState> savedStateList =
                mFolioPageFragmentAdapter.getSavedStateList();
        if (savedStateList != null) {
            for (int i = 0; i < savedStateList.size(); i++) {
                Fragment.SavedState savedState = savedStateList.get(i);
                Bundle bundle = FolioPageFragmentAdapter.getBundleFromSavedState(savedState);
                if (bundle != null)
                    bundle.putParcelable(FolioPageFragment.BUNDLE_SEARCH_ITEM, null);
            }
        }
    }

    private FolioPageFragment getCurrentFragment() {

        if (mFolioPageFragmentAdapter != null && mFolioPageViewPager != null) {
            return (FolioPageFragment) mFolioPageFragmentAdapter
                    .getItem(mFolioPageViewPager.getCurrentItem());
        } else {
            return null;
        }
    }

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onActionModeStarted(android.view.ActionMode mode) {
        super.onActionModeStarted(mode);
    }

    @Nullable
    @Override
    public android.view.ActionMode startActionMode(android.view.ActionMode.Callback callback) {
        return super.startActionMode(callback);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(@NonNull ActionMode.Callback callback) {
        return super.onWindowStartingSupportActionMode(callback);
    }

    @Nullable
    @Override
    public ActionMode startSupportActionMode(@NonNull ActionMode.Callback callback) {
        return super.startSupportActionMode(callback);
    }

    @Nullable
    @Override
    public android.view.ActionMode startActionMode(android.view.ActionMode.Callback callback, int type) {
        return super.startActionMode(callback, type);
    }

    @Nullable
    @Override
    public android.view.ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback) {
        return super.onWindowStartingActionMode(callback);
    }

    @Nullable
    @Override
    public android.view.ActionMode onWindowStartingActionMode(android.view.ActionMode.Callback callback, int type) {
        return super.onWindowStartingActionMode(callback, type);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }


}