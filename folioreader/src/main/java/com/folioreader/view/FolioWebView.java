package com.folioreader.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Scroller;

import com.folioreader.Constants;

/**
 * @author by mahavir on 3/31/16.
 */
public class FolioWebView extends WebView
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String LOG_TAG = FolioWebView.class.getSimpleName();
    private DirectionalViewpager.Direction direction;
    private float TOUCH_SLOP = 0;
    private int current_x = 0;
    private int pageCount = 0;
    private int currentPageIndex = 0;
    private float density;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private GestureDetectorCompat gestureDetector;
    private boolean fakeDragging;
    private MotionEvent eventActionDown;
    private int downScrollX;
    private float MIN_DISTANCE_FOR_FLING;
    private float MIN_DISTANCE_FOR_SCROLL;
    private Scroller scroller;
    private final int PAGE_CHANGE_DURATION = 400;
    private int pageWidthCssPixels;

    private enum TouchDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    private enum MotionType {
        SCROLL,
        FLING
    }

    public FolioWebView(Context context) {
        super(context);
        init();
    }

    public FolioWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolioWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //TODO: -> is scrollToCurrentPage needed?
    public void scrollToCurrentPage() {
        scrollTo(current_x, 0);
    }

    private void init() {

        scroller = new Scroller(getContext());
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        gestureDetector.setOnDoubleTapListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getContext());
        //TODO: -> Check memory leak
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(sharedPreferences, Constants.VIEWPAGER_DIRECTION_KEY);

        density = getResources().getDisplayMetrics().density;
        TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        MIN_DISTANCE_FOR_FLING = 25 * density;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        MIN_DISTANCE_FOR_SCROLL = getResources().getDisplayMetrics().widthPixels * 0.33F;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        hideOrShowToolBar(event);

        if (direction == DirectionalViewpager.Direction.VERTICAL) {
            return computeVerticalScroll(event);
        } else if (direction == DirectionalViewpager.Direction.HORIZONTAL) {
            return computeHorizontalScroll(event);
        }

        throw new IllegalStateException("-> illegal state of direction");
    }

    private boolean computeVerticalScroll(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private boolean computeHorizontalScroll(MotionEvent event) {
        //Log.v(LOG_TAG, "-> computeHorizontalScroll");

        boolean gestureReturn = gestureDetector.onTouchEvent(event);
        Log.v(LOG_TAG, "-> computeHorizontalScroll -> gestureReturn = " + gestureReturn);

        if (gestureReturn)
            return true;

        if (event.getAction() == MotionEvent.ACTION_UP)
            return onScrollUp(event);

        return super.onTouchEvent(event);
    }

    private boolean onScrollUp(MotionEvent event) {

        boolean superReturn = super.onTouchEvent(event);

        if (fakeDragging) {
            Log.v(LOG_TAG, "-> computeHorizontalScroll -> ACTION_UP -> " + event);

            int upScrollX = getScrollX();
            TouchDirection touchDirection = null;

            if (downScrollX < upScrollX) {
                touchDirection = TouchDirection.RIGHT_TO_LEFT;
            } else if (downScrollX > upScrollX) {
                touchDirection = TouchDirection.LEFT_TO_RIGHT;
            }

            currentPageIndex = computeTargetPageIndex(eventActionDown, event, downScrollX,
                    upScrollX, touchDirection, MotionType.SCROLL);
            //TODO: -> duration based on delta
            smoothScrollTo(upScrollX, 0, getPageDeltaX(upScrollX), 0, PAGE_CHANGE_DURATION);

            fakeDragging = false;
        }
        return superReturn;
    }

    //TODO: -> Can be more robust
    private int computeTargetPageIndex(MotionEvent eventActionDown, MotionEvent eventActionUp,
                                       int downScrollX, int upScrollX, TouchDirection touchDirection,
                                       MotionType motionType) {
        Log.d(LOG_TAG, "-> computeTargetPageIndex -> eventActionDown = " + eventActionDown +
                ", eventActionUp = " + eventActionUp + ", downScrollX = " + downScrollX +
                ", upScrollX = " + upScrollX + ", motionType = " + motionType);

        int deltaScrollX = Math.abs(downScrollX - upScrollX);
        int deltaX = (int) Math.abs(eventActionDown.getX() - eventActionUp.getX());

        if (motionType == MotionType.SCROLL && deltaScrollX < MIN_DISTANCE_FOR_SCROLL) {
            Log.d(LOG_TAG, "-> computeTargetPageIndex -> under MIN_DISTANCE_FOR_SCROLL");
            return currentPageIndex;
        } else if (motionType == MotionType.FLING && deltaX < MIN_DISTANCE_FOR_FLING &&
                deltaScrollX < MIN_DISTANCE_FOR_FLING) {
            Log.d(LOG_TAG, "-> computeTargetPageIndex -> under MIN_DISTANCE_FOR_FLING");
            return currentPageIndex;
        }

        float pagePosition = (float) upScrollX / (float) pageWidthCssPixels;
        Log.d(LOG_TAG, "-> computeTargetPageIndex -> pagePosition = " + pagePosition);
        int targetPageIndex;

        if (touchDirection == TouchDirection.RIGHT_TO_LEFT) {
            Log.d(LOG_TAG, "-> computeTargetPageIndex -> touchDirection in right to left direction");
            targetPageIndex = (int) Math.ceil(pagePosition);
        } else if (touchDirection == TouchDirection.LEFT_TO_RIGHT) {
            Log.d(LOG_TAG, "-> computeTargetPageIndex -> touchDirection in left to right direction");
            targetPageIndex = (int) Math.floor(pagePosition);
        } else {
            //TODO: -> check if this throw is required
            throw new IllegalStateException("-> illegal state of touchDirection");
        }

        if (targetPageIndex < pageCount &&
                targetPageIndex >= 0) {
            return targetPageIndex;
        } else {
            return currentPageIndex;
        }
    }

    private int getPageDeltaX(int upScrollX) {
        return getScrollXForPage(currentPageIndex) - upScrollX;
    }

    public int getScrollXForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXForPage -> page = " + page);
        return page * pageWidthCssPixels;
    }

    /**
     * Currently supports only Horizontal scrolling logic.
     */
    private void smoothScrollTo(int startX, int startY, int dx, int dy, int duration) {
        scroller.forceFinished(true);
        scroller.startScroll(startX, startY, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (scroller.computeScrollOffset()) {
            //Log.v(LOG_TAG, "-> computeScroll -> currX = " + scroller.getCurrX() + ", currY = " + scroller.getCurrY());
            scrollTo(scroller.getCurrX(), 0);

            if (Math.abs(scroller.getCurrX() - getScrollXForPage(currentPageIndex)) > 0)
                ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void hideOrShowToolBar(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                eventActionDown = MotionEvent.obtain(event);
                if (mSeekBarListener != null)
                    mSeekBarListener.fadeInSeekBarIfInvisible();
                break;

            case MotionEvent.ACTION_UP:
                if (mToolBarListener != null) {
                    if ((Math.abs(event.getY() - eventActionDown.getY()) < TOUCH_SLOP) &&
                            (Math.abs(event.getX() - eventActionDown.getX()) < TOUCH_SLOP)) {
                        //SingleTap
                        mToolBarListener.hideOrShowToolBar();
                    }
                }
                break;
        }
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
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
        Log.d(LOG_TAG, "-> onDown -> " + event.toString());

        scroller.forceFinished(true);
        eventActionDown = MotionEvent.obtain(event);
        downScrollX = getScrollX();
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(LOG_TAG, "-> onFling -> event1 = " + event1.toString() + ", event2 = "
                + event2.toString() + ", velocityX = " + velocityX + ", velocityY = " + velocityY);

        int upScrollX = getScrollX();
        TouchDirection touchDirection = null;

        if (velocityX < 0) {
            touchDirection = TouchDirection.RIGHT_TO_LEFT;
        } else if (velocityX > 0) {
            touchDirection = TouchDirection.LEFT_TO_RIGHT;
        }

        currentPageIndex = computeTargetPageIndex(event1, event2, downScrollX, upScrollX,
                touchDirection, MotionType.FLING);
        smoothScrollTo(upScrollX, 0, getPageDeltaX(upScrollX), 0, PAGE_CHANGE_DURATION);

        fakeDragging = false;
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //Log.v(LOG_TAG, "-> onLongPress -> " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        Log.v(LOG_TAG, "-> onScroll -> " + event1.toString() + event2.toString() +
                ", distanceX = " + distanceX + ", distanceY = " + distanceY);

        fakeDragging = true;
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
