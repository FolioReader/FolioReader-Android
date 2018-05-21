package com.folioreader.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;

import com.folioreader.util.SharedPreferenceUtil;

/**
 * @author by mahavir on 3/31/16.
 */
public class FolioWebView extends WebView {
    private float MOVE_THRESHOLD_DP = 0;
    private float mDownPosX = 0;
    private float mDownPosY = 0;
    private int current_x = 0;
    private int pageCount = 0;
    private int currentPage = 0;
    private int delta = 30;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private PageChangeListener pageChangeListener = null;

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

    public void scrollToCurrentPage() {
        scrollTo(current_x, 0);
    }

    private void init() {
        MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;
        setDelta();
    }

    public void setPageChangeListener(PageChangeListener pageChangeListener) {
        this.pageChangeListener = pageChangeListener;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        if (SharedPreferenceUtil.getPagerOrientation(getContext())
                .equals(DirectionalViewpager.Direction.VERTICAL.toString())) {
            handleVerticalScrolling(event, action);
        } else {
            Boolean x = handleHorizontalScrolling(event);
            if (x != null) return x;
        }
        return super.onTouchEvent(event);
    }

    private void setDelta() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        delta = (int) (displayMetrics.widthPixels * 0.04);
    }

    @Nullable
    private Boolean handleHorizontalScrolling(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                super.onTouchEvent(event);
                break;
            case MotionEvent.ACTION_DOWN:
                mDownPosX = event.getX();
                if (mSeekBarListener != null) mSeekBarListener.fadeInSeekBarIfInvisible();
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
                float x2 = event.getX();
                float deltaX = x2 - mDownPosX;
                hideOrShowToolBar(event);
                if (Math.abs(deltaX) > delta) {
                    // Left to Right swipe action
                    if (x2 > mDownPosX) {
                        turnPageLeft(deltaX);
                    }
                    // Right to left swipe action
                    else {
                        turnPageRight(deltaX);
                    }
                }
            default:
                super.onTouchEvent(event);
        }
        return null;
    }

    private void hideOrShowToolBar(MotionEvent event) {
        if (mToolBarListener != null &&
                (Math.abs(event.getX() - mDownPosX) < MOVE_THRESHOLD_DP
                        || Math.abs(event.getY() - mDownPosY) < MOVE_THRESHOLD_DP)) {
            mToolBarListener.hideOrShowToolBar();
        }
    }

    private void handleVerticalScrolling(MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPosX = event.getX();
                mDownPosY = event.getY();
                if (mSeekBarListener != null) mSeekBarListener.fadeInSeekBarIfInvisible();
                break;
            case MotionEvent.ACTION_UP:
                hideOrShowToolBar(event);
                break;
        }
    }

    private void turnPageLeft(float deltaX) {
        if (currentPage > 0) {
            int scrollX = getPrevPagePosition();
            loadAnimation(scrollX, deltaX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        } else {
            pageChangeListener.previousPage();
        }
    }

    private int getPrevPagePosition() {
        return (int) Math.ceil(--currentPage * this.getMeasuredWidth());
    }

    private void turnPageRight(float deltaX) {
        if (currentPage < pageCount) {
            int paddingOffset = 10;
            int scrollX = getNextPagePosition();
            loadAnimation(scrollX + paddingOffset, deltaX);
            current_x = scrollX + paddingOffset;
            scrollTo(scrollX + paddingOffset, 0);
        } else {
            pageChangeListener.nextPage();
        }
    }

    private void loadAnimation(int scrollX, float deltaX) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollX",
                current_x - (int) deltaX, scrollX);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private int getNextPagePosition() {
        return (int) Math.ceil(++currentPage * this.getMeasuredWidth());
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

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    public interface PageChangeListener {
        void nextPage();

        void previousPage();
    }

    public interface SeekBarListener {
        void fadeInSeekBarIfInvisible();
    }

    public interface ToolBarListener {
        void hideOrShowToolBar();

        void hideToolBarIfVisible();
    }
}
