package br.com.rsa.folioreader;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by rodrigo.almeida on 08/04/15.
 */


public class FolioReaderViewPager extends ViewPager {

    private GestureDetector mGestureDetector;
    private boolean mIsLockOnHorizontalAxis = false;

    public FolioReaderViewPager(Context context) {
        super(context);
        init(context);
    }

    public FolioReaderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setPageTransformer(true, new ViewPagerTransformer());
        mGestureDetector = new GestureDetector(context, new XScrollDetector());
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        event.setLocation(event.getY(), event.getX());

        // decide if horizontal axis is locked already or we need to check the scrolling direction
        if (!mIsLockOnHorizontalAxis)
            mIsLockOnHorizontalAxis = mGestureDetector.onTouchEvent(event);

        // release the lock when finger is up
        if (event.getAction() == MotionEvent.ACTION_UP)
            mIsLockOnHorizontalAxis = false;

        getParent().requestDisallowInterceptTouchEvent(mIsLockOnHorizontalAxis);
        return super.onTouchEvent(event);
    }

    private class XScrollDetector extends GestureDetector.SimpleOnGestureListener {

        /**
         * @return true - if we're scrolling in X direction, false - in Y direction.
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return Math.abs(distanceX) > Math.abs(distanceY);
        }

    }

    private class ViewPagerTransformer implements PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                view.setAlpha(1);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                //set Y position to swipe in from top
                float yPosition = position * pageHeight;
                view.setTranslationY(yPosition);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }


    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

}
