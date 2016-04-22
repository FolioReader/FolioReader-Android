package com.folioreader.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.folioreader.R;

/**
 * Created by mahavir on 3/31/16.
 */
public class ObservableWebView extends WebView {

    private ActionMode.Callback mActionModeCallback;


    public static interface ScrollListener {
        public void onScrollChange(float percent);
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
        //Log.d("ObservableWebview", "Height: "+height+", WebviewHeight: "+webViewHeight+", scrollY:"+t);
        if (mScrollListener!=null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);
    }


    private class CustomActionModeCallback implements ActionMode.Callback {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown.
        // Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Note: This is called every time the selection handlebars move.
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (R.id.copy == item.getItemId()) {
                Log.d("***copy","in copy");
                // Do some stuff for this button
                mode.finish();
                /// / Action picked, so close the CAB
                return true;
            } else if (R.id.button2 == item.getItemId()) {
                // Do some stuff for this button
                Log.d("***button2","in Button 2");
                mode.finish();
                return true;
            } else {
                mode.finish();
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            clearFocus();
        }
    }

     @Override
        public ActionMode startActionMode(ActionMode.Callback callback) {
            ViewParent parent = getParent();
            if (parent == null) {
                return null;
            }
            mActionModeCallback = new CustomActionModeCallback();
            return parent.startActionModeForChild(this, mActionModeCallback);
        }

}
