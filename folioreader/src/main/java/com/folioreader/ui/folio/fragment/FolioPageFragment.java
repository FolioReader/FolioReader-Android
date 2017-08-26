package com.folioreader.ui.folio.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.bossturban.webviewmarker.TextSelectionSupport;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.Highlight;
import com.folioreader.model.event.AnchorIdEvent;
import com.folioreader.model.event.MediaOverlayHighlightStyleEvent;
import com.folioreader.model.event.MediaOverlayPlayPauseEvent;
import com.folioreader.model.event.MediaOverlaySpeedEvent;
import com.folioreader.model.event.ReloadDataEvent;
import com.folioreader.model.event.RewindIndexEvent;
import com.folioreader.model.event.WebViewPosition;
import com.folioreader.model.quickaction.ActionItem;
import com.folioreader.model.quickaction.QuickAction;
import com.folioreader.ui.base.HtmlTask;
import com.folioreader.ui.base.HtmlTaskCallback;
import com.folioreader.ui.base.HtmlUtil;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.mediaoverlay.MediaController;
import com.folioreader.ui.folio.mediaoverlay.MediaControllerCallbacks;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;
import com.folioreader.util.SMILParser;
import com.folioreader.util.UiUtil;
import com.folioreader.view.ObservableWebView;
import com.folioreader.view.VerticalSeekbar;
import com.squareup.otto.Subscribe;

import org.readium.r2_streamer.model.publication.link.Link;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragment extends Fragment implements HtmlTaskCallback, MediaControllerCallbacks {

    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.ui.folio.fragment.FolioPageFragment.POSITION";
    public static final String KEY_FRAGMENT_FOLIO_BOOK_TITLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.BOOK_TITLE";
    public static final String KEY_FRAGMENT_EPUB_FILE_NAME = "com.folioreader.ui.folio.fragment.FolioPageFragment.EPUB_FILE_NAME";
    private static final String KEY_IS_SMIL_AVAILABLE = "com.folioreader.ui.folio.fragment.FolioPageFragment.IS_SMIL_AVAILABLE";
    public static final String TAG = FolioPageFragment.class.getSimpleName();

    private static final int ACTION_ID_COPY = 1001;
    private static final int ACTION_ID_SHARE = 1002;
    private static final int ACTION_ID_HIGHLIGHT = 1003;
    private static final int ACTION_ID_DEFINE = 1004;

    private static final int ACTION_ID_HIGHLIGHT_COLOR = 1005;
    private static final int ACTION_ID_DELETE = 1006;

    private static final int ACTION_ID_HIGHLIGHT_YELLOW = 1007;
    private static final int ACTION_ID_HIGHLIGHT_GREEN = 1008;
    private static final int ACTION_ID_HIGHLIGHT_BLUE = 1009;
    private static final int ACTION_ID_HIGHLIGHT_PINK = 1010;
    private static final int ACTION_ID_HIGHLIGHT_UNDERLINE = 1011;
    private static final String KEY_TEXT_ELEMENTS = "text_elements";
    private static final String SPINE_ITEM = "spine_item";
    private WebViewPosition mWebviewposition;
    private String mHtmlString = null;
    private boolean hasMediaOverlay = false;
    private String mAnchorId;
    private String rangy = "";

    public interface FolioPageFragmentCallback {

        void hideOrshowToolBar();

        void hideToolBarIfVisible();

        void setPagerToPosition(String href);

        void setLastWebViewPosition(int position);

        void goToChapter(String href);
    }

    private View mRootView;

    private VerticalSeekbar mScrollSeekbar;
    private ObservableWebView mWebview;
    private TextSelectionSupport mTextSelectionSupport;
    private TextView mPagesLeftTextView, mMinutesLeftTextView;
    private FolioPageFragmentCallback mActivityCallback;

    private int mScrollY;
    private int mTotalMinutes;
    private String mSelectedText;
    private Map<String, String> mHighlightMap;
    private Animation mFadeInAnimation, mFadeOutAnimation;

    private Link spineItem;
    private int mPosition = -1;
    private String mBookTitle;
    private String mEpubFileName = null;
    private int mPos;
    private boolean mIsPageReloaded;
    private int mLastWebviewScrollpos;

    private String highlightStyle;

    private MediaController mediaController;
    private Config mConfig;

    public static FolioPageFragment newInstance(int position, String bookTitle, Link spineRef, Config config) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, bookTitle);
        args.putSerializable(SPINE_ITEM, spineRef);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)
                && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_BOOK_TITLE)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = savedInstanceState.getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = savedInstanceState.getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            spineItem = (Link) savedInstanceState.getSerializable(SPINE_ITEM);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = getArguments().getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            spineItem = (Link) getArguments().getSerializable(SPINE_ITEM);
        }
        if (spineItem != null) {
            if (spineItem.properties.contains("media-overlay")) {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.SMIL, this);
                hasMediaOverlay = true;
            } else {
                mediaController = new MediaController(getActivity(), MediaController.MediaType.TTS, this);
                mediaController.setTextToSpeech(getActivity());
            }
        }
        highlightStyle = Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
        mRootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);
        mPagesLeftTextView = (TextView) mRootView.findViewById(R.id.pagesLeft);
        mMinutesLeftTextView = (TextView) mRootView.findViewById(R.id.minutesLeft);
        if (getActivity() instanceof FolioPageFragmentCallback)
            mActivityCallback = (FolioPageFragmentCallback) getActivity();
         mConfig = AppUtil.getSavedConfig(getActivity());

        FolioActivity.BUS.register(this);

        initSeekbar();
        initAnimations();
        initWebView();
        updatePagesLeftTextBg();

        return mRootView;
    }

    private String getWebviewUrl() {
        return Constants.LOCALHOST + mBookTitle + "/" + spineItem.href;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float positionTopView = mWebview.getTop();
        float contentHeight = mWebview.getContentHeight();
        float currentScrollPosition = mScrollY;
        float percentWebview = (currentScrollPosition - positionTopView) / contentHeight;
        float webviewsize = mWebview.getContentHeight() - mWebview.getTop();
        float positionInWV = webviewsize * percentWebview;
        int positionY = Math.round(mWebview.getTop() + positionInWV);
        mScrollY = positionY;
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link FolioActivity#initAudioView()} when pause/play
     * button is clicked
     *
     * @param event of type {@link MediaOverlayPlayPauseEvent} contains if paused/played
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void pauseButtonClicked(MediaOverlayPlayPauseEvent event) {
        if (isAdded()) {
            if (spineItem.href.equals(event.getHref())) {
                mediaController.stateChanged(event);
            }
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link FolioActivity#initAudioView()} when speed
     * change buttons are clicked
     *
     * @param event of type {@link MediaOverlaySpeedEvent} contains selected speed
     *              type HALF,ONE,ONE_HALF and TWO.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void speedChanged(MediaOverlaySpeedEvent event) {
        mediaController.setSpeed(event.getSpeed());
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link FolioActivity#initAudioView()} when new
     * style is selected on button click.
     *
     * @param event of type {@link MediaOverlaySpeedEvent} contains selected style
     *              of type DEFAULT,UNDERLINE and BACKGROUND.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void styleChanged(MediaOverlayHighlightStyleEvent event) {
        if (isAdded()) {
            switch (event.getStyle()) {
                case DEFAULT:
                    highlightStyle =
                            Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.Normal);
                    break;
                case UNDERLINE:
                    highlightStyle =
                            Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.DottetUnderline);
                    break;
                case BACKGROUND:
                    highlightStyle =
                            Highlight.HighlightStyle.classForStyle(Highlight.HighlightStyle.TextColor);
                    break;
            }
            mWebview.loadUrl(String.format(getString(R.string.setmediaoverlaystyle), highlightStyle));
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered when any EBook configuration is changed.
     *
     * @param reloadDataEvent empty POJO.
     */
    @Subscribe
    public void reload(ReloadDataEvent reloadDataEvent) {
        if (isAdded()) {
            mLastWebviewScrollpos = mWebview.getScrollY();
            mIsPageReloaded = true;
            setHtml(true);
            updatePagesLeftTextBg();
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from {@link FolioActivity#onActivityResult(int, int, Intent)} when any item in toc clicked.
     *
     * @param event of type {@link AnchorIdEvent} contains selected chapter href.
     */
    @Subscribe
    public void jumpToAnchorPoint(AnchorIdEvent event) {
        if (isAdded()) {
            if (event != null && event.getHref() != null) {
                String href = event.getHref();
                if (href != null && href.indexOf('#') != -1) {
                    if (spineItem.href.equals(href.substring(0, href.lastIndexOf('#')))) {
                        mAnchorId = href.substring(href.lastIndexOf('#') + 1);
                    }
                }
            }
        }
    }

    @Override
    public void onReceiveHtml(String html) {
        if (isAdded()) {
            mHtmlString = html;
            setHtml(false);
        }
    }

    private void setHtml(boolean reloaded) {
        if (spineItem != null) {
            String ref = spineItem.href;
            if (!reloaded) {
                if (spineItem.properties.contains("media-overlay")) {
                    mediaController.setSMILItems(SMILParser.parseSMIL(mHtmlString));
                    mediaController.setUpMediaPlayer(spineItem.mediaOverlay, spineItem.mediaOverlay.getAudioPath(spineItem.href), mBookTitle);
                }
            }
            mConfig = AppUtil.getSavedConfig(getActivity());
            String path = ref.substring(0, ref.lastIndexOf("/"));
            mWebview.loadDataWithBaseURL(
                    Constants.LOCALHOST + mBookTitle + "/" + path + "/",
                    HtmlUtil.getHtmlContent(getActivity(), mHtmlString, mBookTitle, mConfig),
                    "text/html",
                    "UTF-8",
                    null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaController.stop();
        //TODO save last media overlay item
    }

    private void initWebView() {
        mWebview = (ObservableWebView) mRootView.findViewById(R.id.contentWebView);
        mWebview.setFragment(FolioPageFragment.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setupScrollBar();
        mWebview.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height =
                                (int) Math.floor(mWebview.getContentHeight() * mWebview.getScale());
                        int webViewHeight = mWebview.getMeasuredHeight();
                        mScrollSeekbar.setMaximum(height - webViewHeight);
                    }
                });

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setVerticalScrollBarEnabled(false);
        mWebview.getSettings().setAllowFileAccess(true);

        mWebview.setHorizontalScrollBarEnabled(false);

        mWebview.addJavascriptInterface(this, "Highlight");
        mWebview.setScrollListener(new ObservableWebView.ScrollListener() {
            @Override
            public void onScrollChange(int percent) {
                if (mWebview.getScrollY() != 0) {
                    mScrollY = mWebview.getScrollY();
                    ((FolioActivity) getActivity()).setLastWebViewPosition(mScrollY);
                }
                mScrollSeekbar.setProgressAndThumb(percent);
                updatePagesLeftText(percent);

            }
        });

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (isAdded()) {
                    if (mAnchorId != null)
                        view.loadUrl("javascript:document.getElementById(\"" + mAnchorId + "\").scrollIntoView()");
                    view.loadUrl("javascript:alert(getReadingTime())");
                    if (!hasMediaOverlay) {
                        view.loadUrl("javascript:alert(wrappingSentencesWithinPTags())");
                    }
                    view.loadUrl(String.format(getString(R.string.setmediaoverlaystyle),
                            Highlight.HighlightStyle.classForStyle(
                                    Highlight.HighlightStyle.Normal)));
                    if (mWebviewposition != null) {
                        setWebViewPosition(mWebviewposition.getWebviewPos());
                    } else if (isCurrentFragment()) {
                        setWebViewPosition(AppUtil.getPreviousBookStateWebViewPosition(getActivity(), mBookTitle));
                    } else if (mIsPageReloaded) {
                        setWebViewPosition(mLastWebviewScrollpos);
                        mIsPageReloaded = false;
                    }
                    String rangy = HighlightUtil.generateRangyString(getPageName());
                    if (!rangy.isEmpty()) {
                        view.loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.setHighlights('%s');}", rangy));
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.isEmpty() && url.length() > 0) {
                    if (Uri.parse(url).getScheme().startsWith("highlight")) {
                        final Pattern pattern = Pattern.compile(getString(R.string.pattern));
                        try {
                            String htmlDecode = URLDecoder.decode(url, "UTF-8");
                            Matcher matcher = pattern.matcher(htmlDecode.substring(12));
                            if (matcher.matches()) {
                                double left = Double.parseDouble(matcher.group(1));
                                double top = Double.parseDouble(matcher.group(2));
                                double width = Double.parseDouble(matcher.group(3));
                                double height = Double.parseDouble(matcher.group(4));
                                onHighlight((int) (UiUtil.convertDpToPixel((float) left,
                                        getActivity())),
                                        (int) (UiUtil.convertDpToPixel((float) top,
                                                getActivity())),
                                        (int) (UiUtil.convertDpToPixel((float) width,
                                                getActivity())),
                                        (int) (UiUtil.convertDpToPixel((float) height,
                                                getActivity())));
                            }
                        } catch (UnsupportedEncodingException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    } else {
                        if (url.contains("storage")) {
                            mActivityCallback.setPagerToPosition(url);
                        } else if (url.endsWith(".xhtml") || url.endsWith(".html")) {
                            mActivityCallback.goToChapter(url);
                        } else {
                            // Otherwise, give the default behavior (open in browser)
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        }
                    }
                }
                return true;
            }
        });

        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {

                if (view.getProgress() == 100) {
                    mWebview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("scroll y", "Scrolly" + mScrollY);
                            mWebview.scrollTo(0, mScrollY);
                        }
                    }, 100);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (FolioPageFragment.this.isVisible()) {
                    if (TextUtils.isDigitsOnly(message)) {
                        mTotalMinutes = Integer.parseInt(message);
                    } else {
                        final Pattern pattern = Pattern.compile(getString(R.string.pattern));
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.matches()) {
                            double left = Double.parseDouble(matcher.group(1));
                            double top = Double.parseDouble(matcher.group(2));
                            double width = Double.parseDouble(matcher.group(3));
                            double height = Double.parseDouble(matcher.group(4));
                            showTextSelectionMenu((int) (UiUtil.convertDpToPixel((float) left,
                                    getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) top,
                                            getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) width,
                                            getActivity())),
                                    (int) (UiUtil.convertDpToPixel((float) height,
                                            getActivity())));
                        } else {
                            // to handle TTS playback when highlight is deleted.
                            Pattern p = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
                            if (!p.matcher(message).matches() && (!message.equals("undefined"))) {
                                if (isCurrentFragment()) {
                                    mediaController.speakAudio(message);
                                }
                            }
                        }
                    }
                    result.confirm();
                }
                return true;
            }
        });

        mTextSelectionSupport = TextSelectionSupport.support(getActivity(), mWebview);
        mTextSelectionSupport.setSelectionListener(new TextSelectionSupport.SelectionListener() {
            @Override
            public void startSelection() {
            }

            @Override
            public void selectionChanged(String text) {
                mSelectedText = text;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebview.loadUrl("javascript:alert(getRectForSelectedText())");
                    }
                });
            }

            @Override
            public void endSelection() {

            }
        });

        mWebview.getSettings().setDefaultTextEncodingName("utf-8");
        new HtmlTask(this).execute(getWebviewUrl());
    }

    private void setupScrollBar() {
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), mScrollSeekbar.getProgressDrawable());
        Drawable thumbDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.icons_sroll);
        UiUtil.setColorToImage(getActivity(), mConfig.getThemeColor(), (thumbDrawable));
        mScrollSeekbar.setThumb(thumbDrawable);
    }

    private void initSeekbar() {
        mScrollSeekbar = (VerticalSeekbar) mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable()
                .setColorFilter(getResources()
                                .getColor(R.color.app_green),
                        PorterDuff.Mode.SRC_IN);
    }

    private void updatePagesLeftTextBg() {

        if (mConfig.isNightMode()) {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.parseColor("#131313"));
        } else {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.WHITE);
        }
    }

    private void updatePagesLeftText(int scrollY) {
        try {
            int currentPage = (int) (Math.ceil((double) scrollY / mWebview.getWebViewHeight()) + 1);
            int totalPages =
                    (int) Math.ceil((double) mWebview.getContentHeightVal()
                            / mWebview.getWebViewHeight());
            int pagesRemaining = totalPages - currentPage;
            String pagesRemainingStrFormat =
                    pagesRemaining > 1 ?
                            getString(R.string.pages_left) : getString(R.string.page_left);
            String pagesRemainingStr = String.format(Locale.US,
                    pagesRemainingStrFormat, pagesRemaining);

            int minutesRemaining =
                    (int) Math.ceil((double) (pagesRemaining * mTotalMinutes) / totalPages);
            String minutesRemainingStr;
            if (minutesRemaining > 1) {
                minutesRemainingStr =
                        String.format(Locale.US, getString(R.string.minutes_left),
                                minutesRemaining);
            } else if (minutesRemaining == 1) {
                minutesRemainingStr =
                        String.format(Locale.US, getString(R.string.minute_left),
                                minutesRemaining);
            } else {
                minutesRemainingStr = getString(R.string.less_than_minute);
            }

            mMinutesLeftTextView.setText(minutesRemainingStr);
            mPagesLeftTextView.setText(pagesRemainingStr);
        } catch (java.lang.ArithmeticException exp) {
            Log.d("divide error", exp.toString());
        }
    }

    private void initAnimations() {
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        mFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mScrollSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOutSeekBarIfVisible();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScrollSeekbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void fadeInSeekBarIfInvisible() {
        if (mScrollSeekbar.getVisibility() == View.INVISIBLE ||
                mScrollSeekbar.getVisibility() == View.GONE) {
            mScrollSeekbar.startAnimation(mFadeInAnimation);
        }
    }

    public void fadeOutSeekBarIfVisible() {
        if (mScrollSeekbar.getVisibility() == View.VISIBLE) {
            mScrollSeekbar.startAnimation(mFadeOutAnimation);
        }
    }

    @Override
    public void onDestroyView() {
        mFadeInAnimation.setAnimationListener(null);
        mFadeOutAnimation.setAnimationListener(null);
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_FRAGMENT_FOLIO_POSITION, mPosition);
        outState.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, mBookTitle);
        outState.putString(KEY_FRAGMENT_EPUB_FILE_NAME, mEpubFileName);
        outState.putSerializable(SPINE_ITEM, spineItem);
    }

    public void highlight(Highlight.HighlightStyle style, boolean isCreated) {
        if (isCreated) {
            mWebview.loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", Highlight.HighlightStyle.classForStyle(style)));
        } else {
            mWebview.loadUrl(String.format("javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}", Highlight.HighlightStyle.classForStyle(style)));
        }
    }

    public void highlightRemove() {
        mWebview.loadUrl("javascript:alert(removeThisHighlight())");
    }

    public void showTextSelectionMenu(int x, int y, final int width, final int height) {
        final ViewGroup root =
                (ViewGroup) getActivity().getWindow()
                        .getDecorView().findViewById(android.R.id.content);
        final View view = new View(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setBackgroundColor(Color.TRANSPARENT);

        root.addView(view);

        view.setX(x);
        view.setY(y);
        final QuickAction quickAction =
                new QuickAction(getActivity(), QuickAction.HORIZONTAL);
        quickAction.addActionItem(new ActionItem(ACTION_ID_COPY,
                getString(R.string.copy)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT,
                getString(R.string.highlight)));
        if (!mSelectedText.trim().contains(" ")) {
            quickAction.addActionItem(new ActionItem(ACTION_ID_DEFINE,
                    getString(R.string.define)));
        }
        quickAction.addActionItem(new ActionItem(ACTION_ID_SHARE,
                getString(R.string.share)));
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                root.removeView(view);
                onTextSelectionActionItemClicked(actionId, view, width, height);
            }
        });
        quickAction.show(view, width, height);
    }

    private void onTextSelectionActionItemClicked(int actionId, View view, int width, int height) {
        if (actionId == ACTION_ID_COPY) {
            UiUtil.copyToClipboard(getActivity(), mSelectedText);
            Toast.makeText(getActivity(), getString(R.string.copied), Toast.LENGTH_SHORT).show();
        } else if (actionId == ACTION_ID_SHARE) {
            UiUtil.share(getActivity(), mSelectedText);
        } else if (actionId == ACTION_ID_DEFINE) {
            showDictDialog(mSelectedText);
        } else if (actionId == ACTION_ID_HIGHLIGHT) {
            onHighlight(view, width, height, true);
        }
    }

    private void showDictDialog(String mSelectedText) {
        DictionaryFragment dictionaryFragment = new DictionaryFragment();
        Bundle b = new Bundle();
        b.putString(Constants.SELECTED_WORD, mSelectedText);
        dictionaryFragment.setArguments(b);
        dictionaryFragment.show(getFragmentManager(), DictionaryFragment.class.getName());
    }

    private void onHighlight(int x, int y, int width, int height) {
        final View view = new View(getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setX(x);
        view.setY(y);
        onHighlight(view, width, height, false);
    }

    private void onHighlight(final View view, int width, int height, final boolean isCreated) {
        ViewGroup root =
                (ViewGroup) getActivity().getWindow().
                        getDecorView().findViewById(android.R.id.content);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            root.addView(view);
        } else {
            final int index = parent.indexOfChild(view);
            parent.removeView(view);
            parent.addView(view, index);
        }

        final QuickAction quickAction = new QuickAction(getActivity(), QuickAction.HORIZONTAL);
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_COLOR,
                getResources().getDrawable(R.drawable.colors_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_DELETE,
                getResources().getDrawable(R.drawable.ic_action_discard)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_SHARE,
                getResources().getDrawable(R.drawable.ic_action_share)));
        final ViewGroup finalRoot = root;
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                finalRoot.removeView(view);
                onHighlightActionItemClicked(actionId, view, isCreated);
            }
        });
        quickAction.show(view, width, height);
    }

    private void onHighlightActionItemClicked(int actionId, View view, boolean isCreated) {
        if (actionId == ACTION_ID_HIGHLIGHT_COLOR) {
            onHighlightColors(view, isCreated);
        } else if (actionId == ACTION_ID_SHARE) {
            UiUtil.share(getActivity(), mSelectedText);
        } else if (actionId == ACTION_ID_DELETE) {
            highlightRemove();
        }
    }

    private void onHighlightColors(final View view, final boolean isCreated) {
        ViewGroup root =
                (ViewGroup) getActivity().getWindow()
                        .getDecorView().findViewById(android.R.id.content);
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            root.addView(view);
        } else {
            final int index = parent.indexOfChild(view);
            parent.removeView(view);
            parent.addView(view, index);
        }

        final QuickAction quickAction = new QuickAction(getActivity(), QuickAction.HORIZONTAL);
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_YELLOW,
                getResources().getDrawable(R.drawable.ic_yellow_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_GREEN,
                getResources().getDrawable(R.drawable.ic_green_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_BLUE,
                getResources().getDrawable(R.drawable.ic_blue_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_PINK,
                getResources().getDrawable(R.drawable.ic_pink_marker)));
        quickAction.addActionItem(new ActionItem(ACTION_ID_HIGHLIGHT_UNDERLINE,
                getResources().getDrawable(R.drawable.ic_underline_marker)));
        final ViewGroup finalRoot = root;
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.dismiss();
                finalRoot.removeView(view);
                onHighlightColorsActionItemClicked(actionId, view, isCreated);
            }
        });
        quickAction.show(view);
    }

    private void onHighlightColorsActionItemClicked(int actionId, View view, boolean isCreated) {
        if (actionId == ACTION_ID_HIGHLIGHT_YELLOW) {
            highlight(Highlight.HighlightStyle.Yellow, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_GREEN) {
            highlight(Highlight.HighlightStyle.Green, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_BLUE) {
            highlight(Highlight.HighlightStyle.Blue, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_PINK) {
            highlight(Highlight.HighlightStyle.Pink, isCreated);
        } else if (actionId == ACTION_ID_HIGHLIGHT_UNDERLINE) {
            highlight(Highlight.HighlightStyle.Underline, isCreated);
        }
    }

    @Override
    public void resetCurrentIndex() {
        if (isCurrentFragment()) {
            mWebview.loadUrl("javascript:alert(rewindCurrentIndex())");
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void onReceiveHighlights(String html) {
        if (html != null) {
            rangy = HighlightUtil.createHighlightRangy(html, mBookTitle, getPageName(), mPosition, mWebview.getScrollY(),rangy);
        }
    }

    private String getPageName() {
        return mBookTitle + "$" + spineItem.href;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void setWebView(final WebViewPosition position) {
        if (position.getHref().equals(spineItem.href)) {
            setWebViewPosition(position.getWebviewPos());
        }
    }

    public void setWebViewPosition(final int position) {
        mWebview.post(new Runnable() {
            @Override
            public void run() {
                mWebview.scrollTo(0, position);
            }
        });
    }

    @Override
    public void highLightText(String fragmentId) {
        mWebview.loadUrl(String.format(getString(R.string.audio_mark_id), fragmentId));
    }

    @Override
    public void highLightTTS() {
        mWebview.loadUrl("javascript:alert(getSentenceWithIndex('epub-media-overlay-playing'))");
    }

    //TODO
    @JavascriptInterface
    public void getRemovedHighlightId(String id) {
        if (id != null) {
            //HighLightTable.deleteHighlight(id);
        }
    }

    //TODO
    @JavascriptInterface
    public void getUpdatedHighlightId(String id, String style) {
        if (id != null) {
            //HighLightTable.updateHighlightStyle(id, style);
        }
    }

    @Subscribe
    public void resetCurrentIndex(RewindIndexEvent resetIndex) {
        if (isCurrentFragment()) {
            mWebview.loadUrl("javascript:alert(rewindCurrentIndex())");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebview != null) mWebview.destroy();
    }

    private boolean isCurrentFragment() {
        return isAdded() && ((FolioActivity) getActivity()).getmChapterPosition() == mPos;
    }

    public void setFragmentPos(int pos) {
        mPos = pos;
    }

    @Override
    public void onError() {
    }
}