package com.folioreader.view;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ui.folio.activity.FolioActivityCallback;

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
    private int pageWidthCssPixels;
    private WebViewPager webViewPager;
    private Handler handler;
    private FolioActivityCallback folioActivityCallback;

    private class HorizontalGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(LOG_TAG, "-> onSingleTapUp");
            folioActivityCallback.toggleSystemUI();
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (!webViewPager.isScrolling()) {
                //TODO: -> check for right edge to left flings from right edge
                // Need to complete the scroll as ViewPager thinks these touch events should not
                // scroll it's pages.
                //Log.d(LOG_TAG, "-> onFling -> completing scroll");
                invalidate();
                scrollTo(getScrollXForPage(webViewPager.getCurrentItem()), 0);
            }
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
            Log.d(LOG_TAG, "-> onSingleTapUp");
            folioActivityCallback.toggleSystemUI();
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
        if (folioActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            gestureDetector = new GestureDetectorCompat(getContext(), new HorizontalGestureListener());
        } else {
            gestureDetector = new GestureDetectorCompat(getContext(), new VerticalGestureListener());
        }
        density = getResources().getDisplayMetrics().density;
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

    public void setFolioActivityCallback(FolioActivityCallback folioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        double widthDp = Math.ceil((getMeasuredWidth() / density));
        pageWidthCssPixels = (int) (widthDp * density);
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

    public int getScrollXForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXForPage -> page = " + page);
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
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
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
