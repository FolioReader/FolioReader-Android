package com.folioreader.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.folioreader.R;

public class WebViewPager extends ViewPager {

    private static final String LOG_TAG = WebViewPager.class.getSimpleName();
    private int pageCount;
    private FolioWebView folioWebView;
    private boolean takeOverScrolling;
    private boolean scrolling;

    public WebViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public WebViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
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
                //Log.d(LOG_TAG, "-> onPageSelected -> " + position);
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

    public void setPageCount(int pageCount) {
        //Log.d(LOG_TAG, "-> pageCount = " + pageCount);

        this.pageCount = pageCount;
        setAdapter(new WebViewPagerAdapter());

        if (folioWebView == null)
            folioWebView = ((View) getParent()).findViewById(R.id.folioWebView);
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
            return pageCount;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            View view = new View(container.getContext());
            view.setClickable(false);
            view.setFocusable(false);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
