package com.folioreader.view;

import com.folioreader.activity.FolioActivity;
import com.folioreader.fragments.FolioPageFragment;

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

/**
 * Created by mahavir on 3/31/16.
 */
public class ObservableWebView extends WebView {

    private ActionMode.Callback mActionModeCallback;
    private FolioPageFragment mFolioPageFragment;
    private FolioPageFragment.FolioPageFragmentCallback mActivityCallback;


    public static interface ScrollListener {
        public void onScrollChange(int percent);
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
    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollListener(ScrollListener listener) {
        mScrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        int webViewHeight = this.getMeasuredHeight();


        //float scrollPercent = ((float)t/(height - webViewHeight))*100.0f;
        //Log.d("ObservableWebview", "Height: "+height+", WebviewHeight: "+webViewHeight+", scrollY:"+
        mActivityCallback = (FolioActivity) getContext();
        mActivityCallback.hideToolBarIfVisible();
        // Log.d("in ScrollChange","l"+l+"t"+t);
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public int getContentHeightVal() {
        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        return height;
    }

    public int getWebviewHeight() {
        return this.getMeasuredHeight();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return this.dummyActionMode();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final boolean[] mMoveOccured = new boolean[1];
        final float[] mDownPosX = new float[1];
        final float[] mDownPosY = new float[1];
        // Log.d("dispatchTouchEvent","dispatch touch event");
        final float MOVE_THRESHOLD_DP = 20 * getResources().getDisplayMetrics().density;
        mActivityCallback = (FolioActivity) getContext();
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMoveOccured[0] = false;
                mDownPosX[0] = event.getX();
                mDownPosY[0] = event.getY();
                mFolioPageFragment.removeCallback();
                // mHandler.removeCallbacks(mHideSeekbarRunnable);
                break;
            case MotionEvent.ACTION_UP:
                if (!mMoveOccured[0]) {
                    mActivityCallback.hideOrshowToolBar();
                }

                // mHandler.postDelayed(mHideSeekbarRunnable, 3000);
                mFolioPageFragment.startCallback();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mDownPosX[0]) > MOVE_THRESHOLD_DP || Math.abs(event.getY() - mDownPosY[0]) > MOVE_THRESHOLD_DP) {
                    //mScrollY = mWebview.getScrollY();
                    mMoveOccured[0] = true;
                    mFolioPageFragment.fadeInSeekbarIfInvisible();
                }
                break;
        }

        //mActivityCallback.hideOrshowToolBar();
        return super.dispatchTouchEvent(event);
        //return false;
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
