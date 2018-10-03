package com.folioreader.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.PopupWindow;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ui.folio.activity.FolioActivityCallback;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @author by mahavir on 3/31/16.
 */
public class FolioWebView extends WebView {

    public static final String LOG_TAG = FolioWebView.class.getSimpleName();
    private int horizontalPageCount = 0;
    private DisplayMetrics displayMetrics;
    private float density;
    private ScrollListener mScrollListener;
    private SeekBarListener mSeekBarListener;
    private GestureDetectorCompat gestureDetector;
    private MotionEvent eventActionDown;
    private int pageWidthCssDp;
    private float pageWidthCssPixels;
    private WebViewPager webViewPager;
    private Handler handler;
    private FolioActivityCallback folioActivityCallback;
    private FolioPageFragment parentFragment;

    private ActionMode actionMode;
    private TextSelectionCb textSelectionCb;
    private TextSelectionCb2 textSelectionCb2;
    private Rect selectionRect = new Rect();
    private Rect popupRect = new Rect();
    private PopupWindow popupWindow = new PopupWindow();
    private View viewTextSelection;
    private final int IS_SCROLLING_CHECK_TIMER = 100;
    private final int IS_SCROLLING_CHECK_MAX_DURATION = 10000;
    private int isScrollingCheckDuration;
    private Runnable isScrollingRunnable;
    private int oldScrollX;
    private int oldScrollY;
    private int lastTouchAction;

    private enum LastScrollType {
        USER, PROGRAMMATIC
    }

    private LastScrollType lastScrollType;

    private class HorizontalGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.v(LOG_TAG, "-> onSingleTapUp");
            dismissPopupWindow();
            folioActivityCallback.toggleSystemUI();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (!webViewPager.isScrolling()) {
                // Need to complete the scroll as ViewPager thinks these touch events should not
                // scroll it's pages.
                //Log.d(LOG_TAG, "-> onFling -> completing scroll");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Delayed to avoid inconsistency of scrolling in WebView
                        scrollTo(getScrollXPixelsForPage(webViewPager.getCurrentItem()), 0);
                    }
                }, 100);
            }

            lastScrollType = LastScrollType.USER;
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

    public void dismissPopupWindow() {
        Log.d(LOG_TAG, "-> dismissPopupWindow -> " + parentFragment.spineItem.getHref());
        popupWindow.dismiss();
        selectionRect = new Rect();
        handler.removeCallbacks(isScrollingRunnable);
        isScrollingCheckDuration = 0;
    }

    @Override
    public void destroy() {
        super.destroy();
        Log.d(LOG_TAG, "-> destroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "-> onPause");
        dismissPopupWindow();
    }

    private class VerticalGestureListener
            extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.v(LOG_TAG, "-> onSingleTapUp");
            dismissPopupWindow();
            folioActivityCallback.toggleSystemUI();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.v(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastScrollType = LastScrollType.USER;
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
        displayMetrics = getResources().getDisplayMetrics();
        density = displayMetrics.density;

        if (folioActivityCallback.getDirection() == Config.Direction.HORIZONTAL) {
            gestureDetector = new GestureDetectorCompat(getContext(), new HorizontalGestureListener());
        } else {
            gestureDetector = new GestureDetectorCompat(getContext(), new VerticalGestureListener());
        }

        viewTextSelection = LayoutInflater.from(getContext()).inflate(R.layout.text_selection, null);
        viewTextSelection.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
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

    public void setParentFragment(FolioPageFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    public void setFolioActivityCallback(FolioActivityCallback folioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        pageWidthCssDp = (int) Math.ceil((getMeasuredWidth() / density));
        pageWidthCssPixels = pageWidthCssDp * density;
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

        lastTouchAction = event.getAction();

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

    public int getScrollXDpForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXDpForPage -> page = " + page);
        return page * pageWidthCssDp;
    }

    public int getScrollXPixelsForPage(int page) {
        //Log.v(LOG_TAG, "-> getScrollXPixelsForPage -> page = " + page);
        return (int) Math.ceil(page * pageWidthCssPixels);
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
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        //Log.d(LOG_TAG, "-> scrollTo -> x = " + x);
        lastScrollType = LastScrollType.PROGRAMMATIC;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mScrollListener != null) mScrollListener.onScrollChange(t);
        super.onScrollChanged(l, t, oldl, oldt);

        if (lastScrollType == LastScrollType.USER) {
            //Log.d(LOG_TAG, "-> onScrollChanged -> scroll initiated by user");
            loadUrl(getContext().getString(R.string.make_search_results_invisible));
            parentFragment.searchItemVisible = null;
        }

        lastScrollType = null;
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

    public static boolean onWebViewConsoleMessage(ConsoleMessage cm, String LOG_TAG, String msg) {
        switch (cm.messageLevel()) {
            case LOG:
                Log.v(LOG_TAG, msg);
                return true;
            case DEBUG:
            case TIP:
                Log.d(LOG_TAG, msg);
                return true;
            case WARNING:
                Log.w(LOG_TAG, msg);
                return true;
            case ERROR:
                Log.e(LOG_TAG, msg);
                return true;
        }
        return false;
    }

    private class TextSelectionCb implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(LOG_TAG, "-> onCreateActionMode");
            //mode.getMenuInflater().inflate(R.menu.menu_text_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d(LOG_TAG, "-> onPrepareActionMode");
            loadUrl("javascript:getSelectionRect();");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(LOG_TAG, "-> onActionItemClicked");
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode");
            dismissPopupWindow();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private class TextSelectionCb2 extends ActionMode.Callback2 {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(LOG_TAG, "-> onCreateActionMode");
            //mode.getMenuInflater().inflate(R.menu.menu_text_selection, menu);
            menu.clear();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d(LOG_TAG, "-> onPrepareActionMode");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(LOG_TAG, "-> onActionItemClicked");
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode");
            dismissPopupWindow();
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            Log.d(LOG_TAG, "-> onGetContentRect");
            loadUrl("javascript:getSelectionRect();");
        }
    }

    @Override
    public ActionMode startActionMode(Callback callback) {
        Log.d(LOG_TAG, "-> startActionMode");

        textSelectionCb = new TextSelectionCb();
        actionMode = super.startActionMode(textSelectionCb);
        actionMode.finish();
        return actionMode;

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public ActionMode startActionMode(Callback callback, int type) {
        Log.d(LOG_TAG, "-> startActionMode");

        textSelectionCb2 = new TextSelectionCb2();
        actionMode = super.startActionMode(textSelectionCb2, type);
        actionMode.finish();
        return actionMode;

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback, type);
    }

    @JavascriptInterface
    public void setSelectionRect(final int left, final int top, final int right, final int bottom) {

        Rect currentSelectionRect;
        int newLeft = (int) (left * density);
        int newTop = (int) (top * density);
        int newRight = (int) (right * density);
        int newBottom = (int) (bottom * density);
        currentSelectionRect = new Rect(newLeft, newTop, newRight, newBottom);
        Log.d(LOG_TAG, "-> setSelectionRect -> " + currentSelectionRect);

        computeTextSelectionRect(currentSelectionRect);
    }

    private void computeTextSelectionRect(Rect currentSelectionRect) {
        Log.v(LOG_TAG, "-> computeTextSelectionRect");

        Rect viewportRect = folioActivityCallback.getViewportRect();
        Log.d(LOG_TAG, "-> viewportRect -> " + viewportRect);

        if (!Rect.intersects(viewportRect, currentSelectionRect)) {
            Log.i(LOG_TAG, "-> currentSelectionRect doesn't intersects viewportRect");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                    handler.removeCallbacks(isScrollingRunnable);
                }
            });
            return;
        }
        Log.i(LOG_TAG, "-> currentSelectionRect intersects viewportRect");

        if (selectionRect.equals(currentSelectionRect)) {
            Log.i(LOG_TAG, "-> setSelectionRect -> currentSelectionRect is equal to previous " +
                    "selectionRect so no need to computeTextSelectionRect and show popupWindow again");
            return;
        }

        Log.i(LOG_TAG, "-> setSelectionRect -> currentSelectionRect is not equal to previous " +
                "selectionRect so computeTextSelectionRect and show popupWindow");
        selectionRect = currentSelectionRect;

        Rect aboveSelectionRect = new Rect(viewportRect);
        aboveSelectionRect.bottom = selectionRect.top;
        Rect belowSelectionRect = new Rect(viewportRect);
        //TODO: -> Add the handle heights
        belowSelectionRect.top = selectionRect.bottom + 50;

        //Log.d(LOG_TAG, "-> aboveSelectionRect -> " + aboveSelectionRect);
        //Log.d(LOG_TAG, "-> belowSelectionRect -> " + belowSelectionRect);

        popupRect.left = viewportRect.left;
        // popupRect initialisation for belowSelectionRect
        // TODO -> Explain this more
        popupRect.top = belowSelectionRect.top;
        popupRect.right = popupRect.left + viewTextSelection.getMeasuredWidth();
        popupRect.bottom = popupRect.top + viewTextSelection.getMeasuredHeight();
        //Log.d(LOG_TAG, "-> Pre decision popupRect -> " + popupRect);

        int popupY;
        if (belowSelectionRect.contains(popupRect)) {
            Log.i(LOG_TAG, "-> show below");
            popupY = belowSelectionRect.top;

        } else {

            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top;
            popupRect.bottom = popupRect.top + viewTextSelection.getMeasuredHeight();

            if (aboveSelectionRect.contains(popupRect)) {
                Log.i(LOG_TAG, "-> show above");
                popupY = aboveSelectionRect.bottom - popupRect.height();

            } else {

                Log.i(LOG_TAG, "-> show in middle");
                int popupYDiff = (viewTextSelection.getMeasuredHeight() - selectionRect.height()) / 2;
                popupY = selectionRect.top - popupYDiff;
            }
        }

        int popupXDiff = (viewTextSelection.getMeasuredWidth() - selectionRect.width()) / 2;
        int popupX = selectionRect.left - popupXDiff;

        popupRect.offsetTo(popupX, popupY);
        //Log.d(LOG_TAG, "-> Post decision popupRect -> " + popupRect);

        // Check if popupRect left side is going outside of the viewportRect
        if (popupRect.left < viewportRect.left) {
            popupRect.right += 0 - popupRect.left;
            popupRect.left = 0;
        }

        // Check if popupRect right side is going outside of the viewportRect
        if (popupRect.right > viewportRect.right) {
            int dx = popupRect.right - viewportRect.right;
            popupRect.left -= dx;
            popupRect.right -= dx;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                showTextSelectionPopup();
            }
        });
    }

    private void showTextSelectionPopup() {
        Log.v(LOG_TAG, "-> showTextSelectionPopup");
        Log.d(LOG_TAG, "-> showTextSelectionPopup -> To be laid out popupRect -> " + popupRect);

        popupWindow.dismiss();
        oldScrollX = getScrollX();
        oldScrollY = getScrollY();

        isScrollingRunnable = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(isScrollingRunnable);
                int currentScrollX = getScrollX();
                int currentScrollY = getScrollY();
                boolean inTouchMode = lastTouchAction == MotionEvent.ACTION_DOWN ||
                        lastTouchAction == MotionEvent.ACTION_MOVE;

                if (oldScrollX == currentScrollX && oldScrollY == currentScrollY && !inTouchMode) {
                    Log.i(LOG_TAG, "-> Stopped scrolling, show Popup");
                    popupWindow.dismiss();
                    popupWindow = new PopupWindow(viewTextSelection, WRAP_CONTENT, WRAP_CONTENT);
                    popupWindow.setClippingEnabled(false);
                    popupWindow.showAtLocation(FolioWebView.this, Gravity.NO_GRAVITY,
                            popupRect.left, popupRect.top);
                } else {
                    Log.i(LOG_TAG, "-> Still scrolling, don't show Popup");
                    oldScrollX = currentScrollX;
                    oldScrollY = currentScrollY;
                    isScrollingCheckDuration += IS_SCROLLING_CHECK_TIMER;
                    if (isScrollingCheckDuration < IS_SCROLLING_CHECK_MAX_DURATION)
                        handler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER);
                }
            }
        };

        handler.removeCallbacks(isScrollingRunnable);
        isScrollingCheckDuration = 0;
        handler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER);
    }
}
