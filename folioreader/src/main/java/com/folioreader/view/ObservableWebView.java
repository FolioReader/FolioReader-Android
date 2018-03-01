package com.folioreader.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;

import com.folioreader.Constants;
import com.folioreader.ui.folio.fragment.FolioPageFragment;
import com.folioreader.util.SharedPreferenceUtil;

/**
 * Created by mahavir on 3/31/16.
 */
public class ObservableWebView extends WebView {

    private float mDownPosX = 0;
    private float mDownPosY = 0;
    private float x1 = -1;
    private int pageCount = 0;

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    public interface PageChangeListner {
        void nextPage();
        void previousPage();
    }

    public interface SeekBarListener {
        void fadeInSeekBarIfInvisible();
    }

    public interface ToolBarListener {
        void hideOrshowToolBar();
        void hideToolBarIfVisible();
    }

    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private ToolBarListener mToolBarListener;
    private PageChangeListner pageChangeListner;

    public ObservableWebView(Context context) {
        super(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ObservableWebView(Context context, AttributeSet attrs,
                             int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        final int action = event.getAction();
        float MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;
        if(SharedPreferenceUtil.getPagerOrientation(getContext()).equals(Constants.ORIENTATION.VERTICAL.toString())) {

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownPosX = event.getX();
                    mDownPosY = event.getY();
                    if (mSeekBarListener != null) mSeekBarListener.fadeInSeekBarIfInvisible();
                    break;
                case MotionEvent.ACTION_UP:
                    if (mToolBarListener != null &&
                            (Math.abs(event.getX() - mDownPosX) < MOVE_THRESHOLD_DP
                                    || Math.abs(event.getY() - mDownPosY) < MOVE_THRESHOLD_DP)) {
                        mToolBarListener.hideOrshowToolBar();
                    }
                    break;
            }
        }else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownPosX = event.getX();
                    if (mSeekBarListener != null) mSeekBarListener.fadeInSeekBarIfInvisible();
                    return true;
                case MotionEvent.ACTION_UP:
                    float x2 = event.getX();
                    float deltaX = x2 - mDownPosX;
                    if (mToolBarListener != null &&
                            (Math.abs(event.getX() - mDownPosX) < MOVE_THRESHOLD_DP
                                    || Math.abs(event.getY() - mDownPosY) < MOVE_THRESHOLD_DP)) {
                        mToolBarListener.hideOrshowToolBar();
                    }
                    if (Math.abs(deltaX) > 100) {
                        // Left to Right swipe action
                        if (x2 > mDownPosX) {
                            turnPageLeft();
                            return true;
                        }

                        // Right to left swipe action
                        else {
                            turnPageRight();
                            return true;
                        }

                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
    private int current_x = 0;

    private void turnPageLeft() {
        if (getCurrentPage() > 0) {
            int scrollX = getPrevPagePosition();
            loadAnimation(scrollX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        } else {
            pageChangeListner.previousPage();
        }
    }

    private int getPrevPagePosition() {
        int prevPage = getCurrentPage() - 1;
        return (int) Math.ceil(prevPage * this.getMeasuredWidth());
    }

    private void turnPageRight() {
        if (getCurrentPage() < pageCount - 1) {
            int scrollX = getNextPagePosition();
            loadAnimation(scrollX);
            current_x = scrollX;
            scrollTo(scrollX, 0);
        } else {
            pageChangeListner.nextPage();
        }
    }

    private void loadAnimation(int scrollX) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "scrollX",
                current_x, scrollX);
        anim.setDuration(500);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

    private int getNextPagePosition() {
        int nextPage = getCurrentPage() + 1;
        return (int) Math.ceil(nextPage * this.getMeasuredWidth());
    }

    public int getCurrentPage() {
        return (int) (Math.ceil((double) getScrollX() / this.getMeasuredWidth()));
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
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return this.dummyActionMode();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return this.dummyActionMode();
    }

    public void setPageChangeListner(PageChangeListner pageChangeListner) {
        this.pageChangeListner = pageChangeListner;
    }

    public ActionMode dummyActionMode() {
        return new ActionMode() {
            @Override
            public void setTitle(CharSequence title) {
            }

            @Override
            public void setTitle(int resId) {
            }

            @Override
            public void setSubtitle(CharSequence subtitle) {
            }

            @Override
            public void setSubtitle(int resId) {
            }

            @Override
            public void setCustomView(View view) {
            }

            @Override
            public void invalidate() {
            }

            @Override
            public void finish() {
            }

            @Override
            public Menu getMenu() {
                return null;
            }

            @Override
            public CharSequence getTitle() {
                return null;
            }

            @Override
            public CharSequence getSubtitle() {
                return null;
            }

            @Override
            public View getCustomView() {
                return null;
            }

            @Override
            public MenuInflater getMenuInflater() {
                return null;
            }
        };
    }
}
