package com.folioreader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

/**
 * Created by mahavir on 3/31/16.
 */
public class ObservableWebView extends WebView {

    private FolioPageFragment mFolioPageFragment;
    private FolioPageFragment.FolioPageFragmentCallback mActivityCallback;
    private float mDownPosX = 0;
    private float mDownPosY = 0;

    public interface ScrollListener {
        void onScrollChange(int percent);
    }

    private ScrollListener mScrollListener;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mActivityCallback = (FolioActivity) getContext();

        final int action = event.getAction();
        float MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPosX = event.getX();
                mDownPosY = event.getY();
                mFolioPageFragment.fadeInSeekBarIfInvisible();
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - mDownPosX) < MOVE_THRESHOLD_DP
                        || Math.abs(event.getY() - mDownPosY) < MOVE_THRESHOLD_DP) {
                    mActivityCallback.hideOrshowToolBar();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        mActivityCallback = (FolioActivity) getContext();
        mActivityCallback.hideToolBarIfVisible();
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

    public void setFragment(FolioPageFragment folioPageFragment) {
        mFolioPageFragment = folioPageFragment;
    }
}
