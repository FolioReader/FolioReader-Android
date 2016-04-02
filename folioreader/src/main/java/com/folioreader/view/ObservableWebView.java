package com.folioreader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by mahavir on 3/31/16.
 */
public class ObservableWebView extends WebView {
    public static interface ScrollListener {
        public void onScrollBottom();
        public void onScrollTop();
        public void onScrolling();
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

    public void setScrollListener(ScrollListener listener){
        mScrollListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Log.d("ObservableScrollView", "Left => "+l+", Top => "+t+", Old-Left => "+oldl+", Old-Top => "+oldt);
        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        int webViewHeight = this.getMeasuredHeight();
        if(this.getScrollY() + webViewHeight >= height){
            if (mScrollListener!=null) mScrollListener.onScrollBottom();
        } else if (t == 0){
            if (mScrollListener!=null) mScrollListener.onScrollTop();
        } else {
            if (mScrollListener!=null) mScrollListener.onScrolling();
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
