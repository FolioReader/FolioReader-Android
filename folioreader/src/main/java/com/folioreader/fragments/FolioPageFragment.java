package com.folioreader.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
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
import com.folioreader.R;
import com.folioreader.activity.FolioActivity;
import com.folioreader.model.Highlight;
import com.folioreader.model.ReloadData;
import com.folioreader.model.RewindIndex;
import com.folioreader.model.Sentence;
import com.folioreader.model.WebViewPosition;
import com.folioreader.quickaction.ActionItem;
import com.folioreader.quickaction.QuickAction;
import com.folioreader.smil.TextElement;
import com.folioreader.sqlite.HighLightTable;
import com.folioreader.util.AppUtil;
import com.folioreader.util.FileUtil;
import com.folioreader.util.HighlightUtil;
import com.folioreader.util.UiUtil;
import com.folioreader.view.ObservableWebView;
import com.folioreader.view.VerticalSeekbar;
import com.squareup.otto.Subscribe;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragment extends Fragment {

    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.fragments.FolioPageFragment.POSITION";
    public static final String KEY_FRAGMENT_FOLIO_BOOK_TITLE = "com.folioreader.fragments.FolioPageFragment.BOOK_TITLE";
    public static final String KEY_FRAGMENT_EPUB_FILE_NAME = "com.folioreader.fragments.FolioPageFragment.EPUB_FILE_NAME";
    private static final String KEY_IS_SMIL_AVAILABLE = "com.folioreader.fragments.FolioPageFragment.IS_SMIL_AVAILABLE";
    private static final String KEY_HTML = "com.folioreader.fragments.FolioPageFragment.KEY_HTML";
    public static final String SP_FOLIO_PAGE_FRAGMENT = "com.folioreader.fragments.FolioPageFragment.SP_FOLIO_PAGE_FRAGMENT";
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
    private WebViewPosition mWebviewposition;
    private String mBookTitle;
    private String mHtmlContent;


    public static interface FolioPageFragmentCallback {
        String getChapterHtmlContent(int position);

        void hideOrshowToolBar();

        void hideToolBarIfVisible();

        void setPagerToPosition(String href);

        void setLastWebViewPosition(int position);
    }

    private View mRootView;
    private Context mContext;

    private VerticalSeekbar mScrollSeekbar;
    private ObservableWebView mWebview;
    private TextSelectionSupport mTextSelectionSupport;
    private TextView mPagesLeftTextView, mMinutesLeftTextView;
    private FolioPageFragmentCallback mActivityCallback;

    private int mScrollY;
    private int mTotalMinutes;
    private String mSelectedText;
    private boolean mIsSpeaking = true;
    private Map<String, String> mHighlightMap;
    private Handler mHandler = new Handler();
    private Animation mFadeInAnimation, mFadeOutAnimation;
    private ArrayList<TextElement> mTextElementList;


    private int mPosition = -1;
    private String mEpubFileName = null;
    private boolean mIsSmilAvailable;
    private int mPos;
    private boolean mIsPageReloaded;
    private int mLastWebviewScrollpos;

    public static FolioPageFragment newInstance(int position, String bookTitle, String epubFileName, ArrayList<TextElement> textElementArrayList, boolean isSmileAvailable) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        args.putString(KEY_FRAGMENT_FOLIO_BOOK_TITLE, bookTitle);
        args.putString(KEY_FRAGMENT_EPUB_FILE_NAME, epubFileName);
        args.putParcelableArrayList(KEY_TEXT_ELEMENTS, textElementArrayList);
        args.putBoolean(KEY_IS_SMIL_AVAILABLE, isSmileAvailable);
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
            mIsSmilAvailable = savedInstanceState.getBoolean(KEY_IS_SMIL_AVAILABLE);
            mTextElementList = savedInstanceState.getParcelableArrayList(KEY_TEXT_ELEMENTS);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
            mBookTitle = getArguments().getString(KEY_FRAGMENT_FOLIO_BOOK_TITLE);
            mEpubFileName = getArguments().getString(KEY_FRAGMENT_EPUB_FILE_NAME);
            mIsSmilAvailable = getArguments().getBoolean(KEY_IS_SMIL_AVAILABLE);
            mTextElementList = getArguments().getParcelableArrayList(KEY_TEXT_ELEMENTS);
        }

        mContext = getActivity();
        mRootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);
        mPagesLeftTextView = (TextView) mRootView.findViewById(R.id.pagesLeft);
        mMinutesLeftTextView = (TextView) mRootView.findViewById(R.id.minutesLeft);
        if (getActivity() instanceof FolioPageFragmentCallback)
            mActivityCallback = (FolioPageFragmentCallback) getActivity();

        FolioActivity.BUS.register(this);


        initSeekbar();
        initAnimations();

        if (savedInstanceState == null) {
            mHtmlContent = getHtmlContent(mActivityCallback.getChapterHtmlContent(mPosition));
        } else {
            mHtmlContent = getCustomSharedPrefs().getString(KEY_HTML + mPosition, "");
        }

        initWebView(mHtmlContent);

        updatePagesLeftTextBg();

        return mRootView;
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


    private void initWebView(String htmlContent) {
        mWebview = (ObservableWebView) mRootView.findViewById(R.id.contentWebView);
        mWebview.setFragment(FolioPageFragment.this);

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
                    view.loadUrl("javascript:alert(getReadingTime())");
                    if (!mIsSmilAvailable) {
                        view.loadUrl("javascript:alert(wrappingSentencesWithinPTags())");
                        view.loadUrl(String.format(getString(R.string.setmediaoverlaystyle),
                                Highlight.HighlightStyle.classForStyle(
                                        Highlight.HighlightStyle.Normal)));
                    }


                    if (mWebviewposition != null) {
                        setWebViewPosition(mWebviewposition.getWebviewPos());
                    } else if (!((FolioActivity) getActivity()).isbookOpened() && isCurrentFragment()) {
                        setWebViewPosition(AppUtil.getPreviousBookStateWebViewPosition(mContext, mBookTitle));
                        ((FolioActivity) getActivity()).setIsbookOpened(true);
                    } else if (mIsPageReloaded) {
                        setWebViewPosition(mLastWebviewScrollpos);
                        mIsPageReloaded = false;
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
                Log.d("FolioPageFragment", "Message from js: " + message);
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
                            if (mIsSpeaking && (!message.equals("undefined"))) {
                                if (isCurrentFragment()) {
                                    Sentence sentence = new Sentence(message);
                                    FolioActivity.BUS.post(sentence);
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
        String opfPath
                = AppUtil.getPathOPF(FileUtil.getFolioEpubFolderPath(mEpubFileName), mContext);
        String baseUrl
                = "file://" + FileUtil.getFolioEpubFolderPath(mEpubFileName) + "/" + opfPath + "//";
        mWebview.loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "UTF-8", null);
        ((FolioActivity) getActivity()).setLastWebViewPosition(mScrollY);
    }

    private void initSeekbar() {
        mScrollSeekbar = (VerticalSeekbar) mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable()
                .setColorFilter(getResources()
                                .getColor(R.color.app_green),
                        PorterDuff.Mode.SRC_IN);
    }

    private void updatePagesLeftTextBg() {
        if (Config.getConfig().isNightMode()) {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.parseColor("#131313"));
        } else {
            mRootView.findViewById(R.id.indicatorLayout)
                    .setBackgroundColor(Color.WHITE);
        }
    }

    private void updatePagesLeftText(int scrollY) {
        try {
            int currentPage = (int) (Math.ceil((double) scrollY / mWebview.getWebviewHeight()) + 1);
            int totalPages =
                    (int) Math.ceil((double) mWebview.getContentHeightVal()
                            / mWebview.getWebviewHeight());
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

    private Runnable mHideSeekbarRunnable = new Runnable() {
        @Override
        public void run() {
            fadeoutSeekbarIfVisible();
        }
    };

    public void fadeInSeekbarIfInvisible() {
        if (mScrollSeekbar.getVisibility() == View.INVISIBLE ||
                mScrollSeekbar.getVisibility() == View.GONE) {
            mScrollSeekbar.startAnimation(mFadeInAnimation);
        }
    }

    private void fadeoutSeekbarIfVisible() {
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
        outState.putString(KEY_FRAGMENT_EPUB_FILE_NAME, mEpubFileName);

        getCustomSharedPrefs().edit().putString(KEY_HTML + mPosition, mHtmlContent).apply();
    }

    private SharedPreferences getCustomSharedPrefs() {
        return getActivity().getSharedPreferences(SP_FOLIO_PAGE_FRAGMENT, Context.MODE_PRIVATE);
    }


    @Subscribe
    public void reload(ReloadData reloadData) {
        if (isCurrentFragment()) {
            mLastWebviewScrollpos = mWebview.getScrollY();
            mIsPageReloaded = true;
            final WebView webView = (WebView) mRootView.findViewById(R.id.contentWebView);
            String htmlContent = getHtmlContent(mActivityCallback.getChapterHtmlContent(mPosition));
            String opfPath
                    = AppUtil.getPathOPF(FileUtil.getFolioEpubFolderPath(mEpubFileName), mContext);
            String baseUrl
                    = "file://" + FileUtil.getFolioEpubFolderPath(mEpubFileName) + "/" + opfPath + "//";
            webView.loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "UTF-8", null);
            updatePagesLeftTextBg();

        }

    }

    @Subscribe
    public void highLightString(Integer position) {
        if (isAdded()) {
            if (mTextElementList != null) {
                String src = mTextElementList.get(position.intValue()).getSrc();
                String[] temp = src.split("#");
                String textId = temp[1];
                mWebview.loadUrl(String.format(getString(R.string.audio_mark_id), textId));
            }

        }
    }

    @Subscribe
    public void getTextSentence(Boolean isSpeaking) {
        if (isCurrentFragment()) {
            mIsSpeaking = true;
            mWebview.loadUrl("javascript:alert(getSentenceWithIndex('epub-media-overlay-playing'))");
        }
    }

    @Subscribe
    public void setStyle(String style) {
        if (isAdded()) {
            mWebview.loadUrl(String.format(getString(R.string.setmediaoverlaystyle), style));
        }
    }

    private String getHtmlContent(String htmlContent) {
        String cssPath =
                String.format(getString(R.string.css_tag), "file:///android_asset/Style.css");
        String jsPath =
                String.format(getString(R.string.script_tag),
                        "file:///android_asset/Bridge.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag),
                        "file:///android_asset/jquery-1.8.3.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag),
                        "file:///android_asset/jpntext.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag),
                        "file:///android_asset/rangy-core.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag),
                        "file:///android_asset/rangy-serializer.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag),
                        "file:///android_asset/android.selection.js");
        jsPath =
                jsPath + String.format(getString(R.string.script_tag_method_call),
                        "setMediaOverlayStyleColors('#C0ED72','#C0ED72')");
        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        Config config = Config.getConfig();
        switch (config.getFont()) {
            case 0:
                classes = "andada";
                break;
            case 1:
                classes = "lato";
                break;
            case 2:
                classes = "lora";
                break;
            case 3:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getFontSize()) {
            case 0:
                classes += " textSizeOne";
                break;
            case 1:
                classes += " textSizeTwo";
                break;
            case 2:
                classes += " textSizeThree";
                break;
            case 3:
                classes += " textSizeFour";
                break;
            case 4:
                classes += " textSizeFive";
                break;
            default:
                break;
        }

        htmlContent = htmlContent.replace("<html ", "<html class=\"" + classes + "\" ");
        ArrayList<Highlight> highlights = HighLightTable.getAllHighlights(mBookTitle);
        for (Highlight highlight : highlights) {
            String highlightStr =
                    "<highlight id=\"" + highlight.getHighlightId() +
                            "\" onclick=\"callHighlightURL(this);\" class=\"" +
                            highlight.getType() + "\">" + highlight.getContent() + "</highlight>";
            String searchStr = highlight.getContentPre() +
                    "" + highlight.getContent() + "" + highlight.getContentPost();
            htmlContent = htmlContent.replaceFirst(searchStr, highlightStr);
        }
        return htmlContent;
    }

    public String getSelectedText() {
        return mSelectedText;
    }

    public void highlight(Highlight.HighlightStyle style, boolean isCreated) {
        if (isCreated) {
            mWebview.loadUrl(String.format(getString(R.string.getHighlightString),
                    Highlight.HighlightStyle.classForStyle(style)));
        } else {
            mWebview.loadUrl(String.format(getString(R.string.sethighlightstyle),
                    Highlight.HighlightStyle.classForStyle(style)));
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
        quickAction.addActionItem(new ActionItem(ACTION_ID_DEFINE,
                getString(R.string.define)));
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
            UiUtil.copyToClipboard(mContext, mSelectedText);
            Toast.makeText(mContext, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        } else if (actionId == ACTION_ID_SHARE) {
            UiUtil.share(mContext, mSelectedText);
        } else if (actionId == ACTION_ID_DEFINE) {
            //TODO: Check how to use define
        } else if (actionId == ACTION_ID_HIGHLIGHT) {
            onHighlight(view, width, height, true);
        }
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
            UiUtil.share(mContext, mSelectedText);
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

    @JavascriptInterface
    public void getHighlightJson(String mJsonResponse) {
        if (mJsonResponse != null) {
            mHighlightMap = AppUtil.stringToJsonMap(mJsonResponse);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebview.loadUrl("javascript:alert(getHTML())");
                }
            });
        }
    }

    @JavascriptInterface
    public void getHtmlAndSaveHighlight(String html) {
        if (html != null && mHighlightMap != null) {
            Highlight highlight =
                    HighlightUtil.matchHighlight(html, mHighlightMap.get("id"), mBookTitle, mPosition);
            highlight.setCurrentWebviewScrollPos(mWebview.getScrollY());
            highlight = ((FolioActivity) getActivity()).setCurrentPagerPostion(highlight);
            HighLightTable.insertHighlight(highlight);
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

    @JavascriptInterface
    public void getRemovedHighlightId(String id) {
        if (id != null) {
            HighLightTable.deleteHighlight(id);
        }
    }

    @JavascriptInterface
    public void getUpdatedHighlightId(String id, String style) {
        if (id != null) {
            HighLightTable.updateHighlightStyle(id, style);
        }
    }

    public void removeCallback() {
        mHandler.removeCallbacks(mHideSeekbarRunnable);
    }

    public void startCallback() {
        mHandler.postDelayed(mHideSeekbarRunnable, 3000);
    }

    @Subscribe
    public void resetCurrentIndex(RewindIndex resetIndex) {
        if (isCurrentFragment()) {
            mWebview.loadUrl("javascript:alert(rewindCurrentIndex())");
        }
    }


    private boolean isCurrentFragment() {
        return isAdded() && ((FolioActivity) getActivity()).getmChapterPosition() == mPos;
    }

    public void setFragmentPos(int pos) {
        mPos = pos;
    }

    @Subscribe
    public void setTextElementList(ArrayList<TextElement> textElementList) {
        if (textElementList != null && textElementList.size() > 0) {
            mIsSmilAvailable = true;
            mTextElementList = textElementList;
        }
    }

    @Subscribe
    public void setWebviewToHighlightPos(final WebViewPosition webViewPosition) {
        mWebviewposition = webViewPosition;
        if (isAdded()) {
            setWebViewPosition(mWebviewposition.getWebviewPos());
        }
    }
}