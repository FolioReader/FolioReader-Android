package com.folioreader.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.folioreader.R;

public class WebViewPager extends ViewPager {

    private static final String LOG_TAG = WebViewPager.class.getSimpleName();
    private int horizontalPageCount;
    private FolioWebView folioWebView;
    private boolean takeOverScrolling;
    private boolean scrolling;
    private Handler handler;
    private GestureDetectorCompat gestureDetector;

    private enum LastGestureType {
        OnSingleTapUp, OnLongPress, OnFling, OnScroll
    }

    private LastGestureType lastGestureType;

    public WebViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public WebViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        handler = new Handler();
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d(LOG_TAG, "-> onPageScrolled -> position = " + position +
//                        ", positionOffset = " + positionOffset + ", positionOffsetPixels = " + positionOffsetPixels);

                scrolling = true;

                if (takeOverScrolling && folioWebView != null) {
                    int scrollX = folioWebView.getScrollXPixelsForPage(position) + positionOffsetPixels;
                    //Log.d(LOG_TAG, "-> onPageScrolled -> scrollX = " + scrollX);
                    folioWebView.scrollTo(scrollX, 0);
                }

                if (positionOffsetPixels == 0) {
                    //Log.d(LOG_TAG, "-> onPageScrolled -> takeOverScrolling = false");
                    takeOverScrolling = false;
                    scrolling = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(LOG_TAG, "-> onPageSelected -> " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public boolean isScrolling() {
        return scrolling;
    }

    private String getScrollStateString(int state) {
        switch (state) {
            case SCROLL_STATE_IDLE:
                return "SCROLL_STATE_IDLE";
            case SCROLL_STATE_DRAGGING:
                return "SCROLL_STATE_DRAGGING";
            case SCROLL_STATE_SETTLING:
                return "SCROLL_STATE_SETTLING";
            default:
                return "UNKNOWN_STATE";
        }
    }

    public void setHorizontalPageCount(int horizontalPageCount) {
        //Log.d(LOG_TAG, "-> horizontalPageCount = " + horizontalPageCount);

        this.horizontalPageCount = horizontalPageCount;
        setAdapter(new WebViewPagerAdapter());
        setCurrentItem(0);

        if (folioWebView == null)
            folioWebView = ((View) getParent()).findViewById(R.id.folioWebView);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setCurrentPage(final int pageIndex) {
        Log.v(LOG_TAG, "-> setCurrentItem -> pageIndex = " + pageIndex);

        handler.post(new Runnable() {
            @Override
            public void run() {
                setCurrentItem(pageIndex, false);
            }
        });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setPageToLast() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                setCurrentItem(horizontalPageCount - 1);
            }
        });
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setPageToFirst() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                setCurrentItem(0);
            }
        });
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            WebViewPager.super.onTouchEvent(e);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            //Log.d(LOG_TAG, "-> onSingleTapUp");
            lastGestureType = LastGestureType.OnSingleTapUp;
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            //Log.d(LOG_TAG, "-> onLongPress -> " + e);
            lastGestureType = LastGestureType.OnLongPress;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.v(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastGestureType = LastGestureType.OnScroll;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastGestureType = LastGestureType.OnFling;
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        boolean gestureReturn = gestureDetector.onTouchEvent(event);
        if (gestureReturn)
            return true;

        boolean superReturn = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (lastGestureType == LastGestureType.OnScroll ||
                    lastGestureType == LastGestureType.OnFling) {
                //Log.d(LOG_TAG, "-> onTouchEvent -> takeOverScrolling = true, " + "lastGestureType = " + lastGestureType);
                takeOverScrolling = true;
            }
            lastGestureType = null;
        }

        return superReturn;
    }

    private class WebViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return horizontalPageCount;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            View view = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.view_webview_pager, container, false);

            // Debug code
            // Set alpha for folioWebView in folio_page_fragment.xml to 0.5 also.
            /*if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.green_border_background);
            } else {
                view.setBackgroundResource(R.drawable.blue_border_background);
            }

            TextView textView = view.findViewById(R.id.textView);
            textView.setText(Integer.toString(position));*/

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
