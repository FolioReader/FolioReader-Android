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
public class FolioWebView extends WebView
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String LOG_TAG = FolioWebView.class.getSimpleName();
    private float touchSlop;
    private int horizontalPageCount = 0;
    private float density;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private GestureDetectorCompat gestureDetector;
    private MotionEvent eventActionDown;
    private int pageWidthCssPixels;
    private WebViewPager webViewPager;
    private Handler handler;
    private FolioActivityCallback folioActivityCallback;

    public FolioWebView(Context context) {
        super(context);
        if (!isInEditMode())
            init();
    }

    public FolioWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init();
    }

    public FolioWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode())
            init();
    }

    private void init() {

        handler = new Handler();
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        gestureDetector.setOnDoubleTapListener(this);
        density = getResources().getDisplayMetrics().density;
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
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

    public void setToolBarListener(ToolBarListener listener) {
        mToolBarListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        hideOrShowToolBar(event);

        if (folioActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            return computeHorizontalScroll(event);
        } else {
            return computeVerticalScroll(event);
        }
    }

    private boolean computeVerticalScroll(MotionEvent event) {
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

    private void hideOrShowToolBar(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                eventActionDown = MotionEvent.obtain(event);
                if (mSeekBarListener != null)
                    mSeekBarListener.fadeInSeekBarIfInvisible();
                break;

            case MotionEvent.ACTION_UP:
                if (mToolBarListener != null &&
                        ((Math.abs(event.getY() - eventActionDown.getY()) < touchSlop) &&
                                (Math.abs(event.getX() - eventActionDown.getX()) < touchSlop))) {
                    //SingleTap
                    mToolBarListener.hideOrShowToolBar();
                }
                break;
        }
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
        if (mToolBarListener != null) mToolBarListener.hideToolBarIfVisible();
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public int getContentHeightVal() {
        return (int) Math.floor(this.getContentHeight() * this.getScale());
    }

    public int getWebViewHeight() {
        return this.getMeasuredHeight();
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onDown -> " + event.toString());

        eventActionDown = MotionEvent.obtain(event);
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {

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
    public void onLongPress(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onLongPress -> " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        //Log.v(LOG_TAG, "-> onScroll -> " + event1.toString() + event2.toString() + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onShowPress -> " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onSingleTapUp -> " + event.toString());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onDoubleTap -> " + event.toString());
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onDoubleTapEvent -> " + event.toString());
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onSingleTapConfirmed -> " + event.toString());
        return false;
    }

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    public interface SeekBarListener {
        void fadeInSeekBarIfInvisible();
    }

    public interface ToolBarListener {
        void hideOrShowToolBar();

        void hideToolBarIfVisible();
    }
}
