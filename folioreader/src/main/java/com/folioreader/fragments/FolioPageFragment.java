package com.folioreader.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.view.ObservableWebView;
import com.folioreader.view.VerticalSeekbar;

import java.util.Locale;

import nl.siegmann.epublib.util.StringUtil;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragment extends Fragment {
    public static final String KEY_FRAGMENT_FOLIO_POSITION = "com.folioreader.fragments.FolioPageFragment.POSITION";

    private View mRootView;

    private VerticalSeekbar mScrollSeekbar;
    private ObservableWebView mWebview;
    private TextView mPagesLeftTextView, mMinutesLeftTextView;

    private int mScrollY;
    private int mTotalMinutes;

    private Handler mHandler = new Handler();
    private Animation mFadeInAnimation, mFadeOutAnimation;

    public static FolioPageFragment newInstance(int position) {
        FolioPageFragment fragment = new FolioPageFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FRAGMENT_FOLIO_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static interface FolioPageFragmentCallback {
        public String getChapterHtmlContent(int position);
        public void hideOrshowToolBar();
        public void hideToolBarIfVisible();
    }

    private int mPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_FRAGMENT_FOLIO_POSITION)) {
            mPosition = savedInstanceState.getInt(KEY_FRAGMENT_FOLIO_POSITION);
        } else {
            mPosition = getArguments().getInt(KEY_FRAGMENT_FOLIO_POSITION);
        }

        mRootView = View.inflate(getActivity(), R.layout.folio_page_fragment, null);
        mPagesLeftTextView = (TextView) mRootView.findViewById(R.id.pagesLeft);
        mMinutesLeftTextView = (TextView) mRootView.findViewById(R.id.minutesLeft);

        initSeekbar();
        initAnimations();
        initWebView();
        updatePagesLeftTextBg();

        return mRootView;
    }

    private void initWebView(){
        String htmlContent = getHtmlContent();

        mWebview = (ObservableWebView) mRootView.findViewById(R.id.contentWebView);
        final Boolean[] mMoveOccured = new Boolean[1];
        final float[] mDownPosX = new float[1];
        final float[] mDownPosY = new float[1];
        mWebview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final float MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;

                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mMoveOccured[0] = false;
                        mDownPosX[0] = event.getX();
                        mDownPosY[0] = event.getY();
                        mHandler.removeCallbacks(mHideSeekbarRunnable);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mMoveOccured[0]) {
                            ((FolioPageFragmentCallback)getActivity()).hideOrshowToolBar();
                        }
                        mHandler.postDelayed(mHideSeekbarRunnable, 3000);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - mDownPosX[0]) > MOVE_THRESHOLD_DP || Math.abs(event.getY() - mDownPosY[0]) > MOVE_THRESHOLD_DP) {
                            mScrollY = mWebview.getScrollY();
                            ((FolioPageFragmentCallback) getActivity()).hideToolBarIfVisible();
                            mMoveOccured[0] = true;
                            fadeInSeekbarIfInvisible();
                        }
                        break;
                }
                return false;
            }
        });
        mWebview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = (int) Math.floor(mWebview.getContentHeight() * mWebview.getScale());
                int webViewHeight = mWebview.getMeasuredHeight();
                mScrollSeekbar.setMaximum(height-webViewHeight);
                //updatePagesLeftText(0);
            }
        });
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setVerticalScrollBarEnabled(false);
        mWebview.setScrollListener(new ObservableWebView.ScrollListener() {
            @Override
            public void onScrollChange(int percent) {
                mScrollSeekbar.setProgressAndThumb((int)percent);
                updatePagesLeftText(percent);

            }
        });
        mWebview.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:alert(getReadingTime())");
            }
        });
        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {

                if (view.getProgress() == 100) {
                    mWebview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mWebview.scrollTo(0, mScrollY);
                        }
                    }, 100);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (TextUtils.isDigitsOnly(message)) mTotalMinutes=Integer.parseInt(message);
                result.confirm();
                return true;
            }
        });

        mWebview.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null);

    }

    private void initSeekbar(){
        mScrollSeekbar = (VerticalSeekbar)mRootView.findViewById(R.id.scrollSeekbar);
        mScrollSeekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.app_green), PorterDuff.Mode.SRC_IN);

    }

    private void updatePagesLeftTextBg(){
        if (Config.getConfig().isNightMode()){
            mRootView.findViewById(R.id.indicatorLayout).setBackgroundColor(Color.parseColor("#131313"));
        } else {
            mRootView.findViewById(R.id.indicatorLayout).setBackgroundColor(Color.WHITE);
        }
    }

    private void updatePagesLeftText(int scrollY){
        int currentPage = (int)Math.ceil(scrollY/mWebview.getWebviewHeight()) + 1;
        int totalPages = (int)Math.ceil(mWebview.getContentHeightVal()/mWebview.getWebviewHeight());
        int pagesRemaining = totalPages-currentPage;
        String pagesRemainingStrFormat = pagesRemaining>1?getString(R.string.pages_left):getString(R.string.page_left);
        String pagesRemainingStr = String.format(Locale.US, pagesRemainingStrFormat, pagesRemaining);

        int minutesRemaining = (int) Math.ceil((double)(pagesRemaining*mTotalMinutes)/totalPages);
        String minutesRemainingStr;
        if (minutesRemaining>1){
            minutesRemainingStr = String.format(Locale.US, getString(R.string.minutes_left), minutesRemaining);
        } else if (minutesRemaining == 1){
            minutesRemainingStr = String.format(Locale.US, getString(R.string.minute_left), minutesRemaining);
        } else {
            minutesRemainingStr = getString(R.string.less_than_minute);
        }

        mMinutesLeftTextView.setText(minutesRemainingStr);
        mPagesLeftTextView.setText(pagesRemainingStr);
    }

    private void initAnimations(){
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

    private void fadeInSeekbarIfInvisible(){
        if (mScrollSeekbar.getVisibility() == View.INVISIBLE || mScrollSeekbar.getVisibility() == View.GONE) {
            mScrollSeekbar.startAnimation(mFadeInAnimation);
        }
    }

    private void fadeoutSeekbarIfVisible(){
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
    }

    public void reload() {
        final WebView webView = (WebView) mRootView.findViewById(R.id.contentWebView);
        String htmlContent = getHtmlContent();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null);

        updatePagesLeftTextBg();
    }

    private String getHtmlContent() {
        String htmlContent = "???";
        if (getActivity() instanceof FolioPageFragmentCallback && mPosition != -1) {
            htmlContent = ((FolioPageFragmentCallback) getActivity()).getChapterHtmlContent(mPosition);
        }

        String cssPath = String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">", "file:///android_asset/Style.css");
        String jsPath = String.format("<script type=\"text/javascript\" src=\"%s\"></script>", "file:///android_asset/Bridge.js");
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
        return htmlContent;
    }

}
