package com.folioreader.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ui.folio.activity.FolioActivityCallback;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

/**
 * @author by mahavir on 3/31/16.
 */
public class FolioWebView extends WebView {

    private static final String LOG_TAG = FolioWebView.class.getSimpleName();
    private int horizontalPageCount = 0;
    private float density;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private GestureDetectorCompat gestureDetector;
    private MotionEvent eventActionDown;
    private int pageWidthCssDp;
    private int pageWidthCssPixels;
    private WebViewPager webViewPager;
    private Handler handler;
    private FolioActivityCallback folioActivityCallback;
    private FolioPageFragment parentFragment;

    private enum LastScrollType {
        USER, PROGRAMMATIC
    }

    private LastScrollType lastScrollType;

    private class HorizontalGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.v(LOG_TAG, "-> onSingleTapUp");
            folioActivityCallback.toggleSystemUI();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (!webViewPager.isScrolling()) {
                // Need to complete the scroll as ViewPager thinks these touch events should not
                // scroll it's pages.
                //Log.d(LOG_TAG, "-> onFling -> completing scroll");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Delayed to avoid inconsistency of scrolling in WebView
                        scrollTo(getScrollXPixelsForPage(webViewPager.getCurrentItem()), 0);
                    }
                }, 100);
            }

            lastScrollType = LastScrollType.USER;
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            //Log.v(LOG_TAG, "-> onDown -> " + event.toString());

            eventActionDown = MotionEvent.obtain(event);
            FolioWebView.super.onTouchEvent(event);
            return true;
        }
    }

    private class VerticalGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.v(LOG_TAG, "-> onSingleTapUp");
            folioActivityCallback.toggleSystemUI();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.v(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastScrollType = LastScrollType.USER;
            return false;
        }
    }

    public FolioWebView(Context context) {
        super(context);
    }

    public FolioWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FolioWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {

        handler = new Handler();
        density = getResources().getDisplayMetrics().density;

        if (folioActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            gestureDetector = new GestureDetectorCompat(getContext(), new HorizontalGestureListener());
        } else {
            gestureDetector = new GestureDetectorCompat(getContext(), new VerticalGestureListener());
        }
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setCompatMode(String compatMode) {
        Log.v(LOG_TAG, "-> setCompatMode -> compatMode = " + compatMode);
        if (compatMode.equals(getContext().getString(R.string.back_compat))) {
            Log.e(LOG_TAG, "-> Web page loaded in Quirks mode. Please report to developer " +
                    "for debugging with current EPUB file as many features might stop working " +
                    "(ex. Horizontal scroll feature).");
        }
    }

    public void setParentFragment(FolioPageFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public void setFolioActivityCallback(FolioActivityCallback folioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        pageWidthCssDp = (int) Math.ceil((getMeasuredWidth() / density));
        pageWidthCssPixels = (int) (pageWidthCssDp * density);
    }

    public void setScrollListener(ScrollListener listener) {
        mScrollListener = listener;
    }

    public void setSeekBarListener(SeekBarListener listener) {
        mSeekBarListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        if (folioActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            return computeHorizontalScroll(event);
        } else {
            return computeVerticalScroll(event);
        }
    }

    private boolean computeVerticalScroll(MotionEvent event) {

        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean computeHorizontalScroll(MotionEvent event) {
        //Log.v(LOG_TAG, "-> computeHorizontalScroll");

        webViewPager.dispatchTouchEvent(event);
        boolean gestureReturn = gestureDetector.onTouchEvent(event);
        if (gestureReturn)
            return true;
        return super.onTouchEvent(event);
    }

    public int getScrollXDpForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXDpForPage -> page = " + page);
        return page * pageWidthCssDp;
    }

    public int getScrollXPixelsForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXPixelsForPage -> page = " + page);
        return page * pageWidthCssPixels;
    }

    public void setHorizontalPageCount(int horizontalPageCount) {
        this.horizontalPageCount = horizontalPageCount;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (webViewPager == null)
                    webViewPager = ((View) getParent()).findViewById(R.id.webViewPager);

                webViewPager.setHorizontalPageCount(FolioWebView.this.horizontalPageCount);
            }
        });
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        //Log.d(LOG_TAG, "-> scrollTo -> x = " + x);
        lastScrollType = LastScrollType.PROGRAMMATIC;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);

        if (lastScrollType == LastScrollType.USER) {
            //Log.d(LOG_TAG, "-> onScrollChanged -> scroll initiated by user");
            loadUrl(getContext().getString(R.string.make_search_results_invisible));
            parentFragment.searchItemVisible = null;
        }

        lastScrollType = null;
    }

    public int getContentHeightVal() {
        return (int) Math.floor(this.getContentHeight() * this.getScale());
    }

    public int getWebViewHeight() {
        return this.getMeasuredHeight();
    }

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    public interface SeekBarListener {
        void fadeInSeekBarIfInvisible();
    }
}
