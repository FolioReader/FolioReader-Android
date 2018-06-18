package com.folioreader.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.TextView;

import com.folioreader.R;

public class WebViewPager extends ViewPager {

    private static final String LOG_TAG = WebViewPager.class.getSimpleName();
    private int horizontalPageCount;
    private FolioWebView folioWebView;
    private boolean takeOverScrolling;
    private boolean scrolling;
    private Handler handler;

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

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.d(LOG_TAG, "-> onPageScrolled -> position = " + position +
//                        ", positionOffset = " + positionOffset + ", positionOffsetPixels = " + positionOffsetPixels);

                scrolling = true;

                if (takeOverScrolling && folioWebView != null) {
                    int scrollX = folioWebView.getScrollXForPage(position) + positionOffsetPixels;
                    //Log.d(LOG_TAG, "-> onPageScrolled -> scrollX = " + scrollX);
                    folioWebView.scrollTo(scrollX, 0);
                }

                if (positionOffsetPixels == 0) {
                    takeOverScrolling = false;
                    scrolling = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(LOG_TAG, "-> onPageSelected -> " + position);
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

        if (folioWebView == null)
            folioWebView = ((View) getParent()).findViewById(R.id.folioWebView);
    }

    @SuppressWarnings("unused")
    @JavascriptInterface
    public void setCurrentPage(final int pageIndex) {
        Log.d(LOG_TAG, "-> setCurrentItem -> pageIndex = " + pageIndex);

        handler.post(new Runnable() {
            @Override
            public void run() {
                folioWebView.invalidate();
                setCurrentItem(pageIndex, false);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        boolean superReturn = super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP)
            takeOverScrolling = true;

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

            if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.green_border_background);
            } else {
                view.setBackgroundResource(R.drawable.blue_border_background);
            }

            TextView textView = view.findViewById(R.id.textView);
            textView.setText(Integer.toString(position));

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
