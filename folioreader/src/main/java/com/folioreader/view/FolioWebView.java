package com.folioreader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import com.folioreader.Constants;
import com.folioreader.R;

/**
 * @author by mahavir on 3/31/16.
 */
public class FolioWebView extends WebView
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String LOG_TAG = FolioWebView.class.getSimpleName();
    private DirectionalViewpager.Direction direction;
    private float touchSlop = 0;
    private int pageCount = 0;
    private float density;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private GestureDetectorCompat gestureDetector;
    private MotionEvent eventActionDown;
    private int pageWidthCssPixels;
    private WebViewPager webViewPager;
    private Handler handler;

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

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, Constants.VIEWPAGER_DIRECTION_KEY);

        density = getResources().getDisplayMetrics().density;
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case Constants.VIEWPAGER_DIRECTION_KEY:
                String directionString = sharedPreferences.getString(key,
                        DirectionalViewpager.Direction.VERTICAL.toString());
                //Log.v(LOG_TAG, "-> onSharedPreferenceChanged -> key: " + key + " value: " + directionString);
                if (directionString.equals(DirectionalViewpager.Direction.VERTICAL.toString())) {
                    direction = DirectionalViewpager.Direction.VERTICAL;
                } else {
                    direction = DirectionalViewpager.Direction.HORIZONTAL;
                }
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        hideOrShowToolBar(event);

        if (direction == DirectionalViewpager.Direction.HORIZONTAL) {
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

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (webViewPager == null)
                    webViewPager = ((View) getParent()).findViewById(R.id.webViewPager);

                webViewPager.setPageCount(FolioWebView.this.pageCount);
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
