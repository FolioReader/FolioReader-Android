package br.com.rsa.folioreader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import br.com.rsa.folioreader.entities.FingerPairDirection;
import br.com.rsa.folioreader.enummerators.ScrollDirection;

/**
 * Created by rodrigo.almeida on 10/04/15.
 */
    public class FolioReaderWebView extends WebView {

    private static final String DEBUG_TAG = "Velocity";
    private VelocityTracker mVelocityTracker = null;

    private int totalIndex;
    private int currentIndex;
    private float downYValue = 0;
    private float downXValue = 0;
    private FingerPairDirection fingerPairDirection;

    public FolioReaderWebView(Context context) {
        super(context);
        init();
    }

    public FolioReaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.getSettings().setJavaScriptEnabled(true);
        this.setWebViewClient(new WebViewClient() {
        });
        fingerPairDirection = new FingerPairDirection();
    }

    public boolean canScrollDown() {
        return this.canScrollVertically(1);
    }

    public boolean isVerticalScrollEnd() {
        return getScrollY() >= getContentHeight();
    }

    public boolean isVerticalScrollBegin() {
        return getScrollY() == 0;
    }

    public boolean canScrollUp() {
        return this.canScrollVertically(-1);
    }

    public void setIntentScroll(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downXValue = event.getX();
                downYValue = event.getY();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                if (downXValue < event.getX())
                    fingerPairDirection.setX(ScrollDirection.Right);
                else if (downXValue > event.getX())
                    fingerPairDirection.setX(ScrollDirection.Left);
                else
                    fingerPairDirection.setX(ScrollDirection.None);

                if (downYValue < event.getY())
                    fingerPairDirection.setY(ScrollDirection.Down);
                else if (downYValue > event.getY())
                    fingerPairDirection.setY(ScrollDirection.Up);
                else
                    fingerPairDirection.setY(ScrollDirection.None);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
    }

    public ScrollDirection getCurrentDirectionVerticalScroll() {
        return fingerPairDirection.getY();
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
    }

    @Override
    public int getContentHeight() {
        return this.computeVerticalScrollRange() - this.getHeight();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {


//        setIntentScroll(event);
//
//        if (isVerticalScrollEnd() && getCurrentDirectionVerticalScroll() == ScrollDirection.Up && event.getAction() == MotionEvent.ACTION_MOVE) {
//            return false;
//        }
//        else
            return super.onTouchEvent(event);

        /*
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if ((canScrollDown() && !isVerticalScrollEnd()) ||
                        (!canScrollDown() && isVerticalScrollEnd() && getIntentScroll(event).getY() == ScrollDirection.Down))
                    return super.onTouchEvent(event);
                else
                    return false;

            case MotionEvent.ACTION_DOWN:
                return super.onTouchEvent(event);

            default:
                return super.onTouchEvent(event);

            //if (isVerticalScrollEnd() && getIntentScroll(event).getY() == ScrollDirection.Up) return false;
        }
        /*
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                downXValue = event.getX();
                downYValue = event.getY();
                break;
            }
        }

        fingerPairDirection = FingerUtils.getPairDirection(event);

        if ((this.canScrollVertically(+1) && getScrollY() < heightContext) ||
                (!this.canScrollVertically(+1) &&
                        getScrollY() >= heightContext &&
                        downYValue <= event.getY()))
            return super.onTouchEvent(event);
        else
            return false;

        /*
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                downXValue = event.getX();
                downYValue = event.getY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float currentX = event.getX();
                float currentY = event.getY();

                if (downXValue < currentX) {
                    fingerPairDirection.setX(ScrollDirection.Right);
                }

                if (downXValue > currentX) {
                    fingerPairDirection.setX(ScrollDirection.Left);
                }

                if (downYValue < currentY) {
                    fingerPairDirection.setY(ScrollDirection.Down);
                }
                if (downYValue > currentY) {
                    fingerPairDirection.setY(ScrollDirection.Up);
                }

                if ((this.canScrollVertically(+1) && getScrollY() < heightContext) ||
                        (!this.canScrollVertically(+1) &&
                                getScrollY() >= heightContext &&
                                FingerUtils.getPairDirection(event).getY() == ScrollDirection.Down))
                    return super.onTouchEvent(event);
                else
                    return false;
            }

        }
        return super.onTouchEvent(event);

        /*
        //Inicio do WebView e ViewPager;
        if (scrollDirection == ScrollDirection.Down && getCurrentIndex() == 0 && getScrollY() == 0) {
            this.requestDisallowInterceptTouchEvent(false);
            return super.onTouchEvent(event);
        }

        //Fim do WebView e ViewPager;
        if (scrollDirection == ScrollDirection.Up && getTotalIndex() == getCurrentIndex() + 1 && this.getScrollY() == heightContext) {
            this.requestDisallowInterceptTouchEvent(false);
            return super.onTouchEvent(event);
        }

        //Next page;
        if (getScrollY() >= heightContext && scrollDirection == ScrollDirection.Up && getCurrentIndex()+1 != getTotalIndex()) {
            this.requestDisallowInterceptTouchEvent(true);
            return false;
        }

        //Previus page;
        if (getScrollY() == 0 && scrollDirection == ScrollDirection.Down && getCurrentIndex() > 0) {
            this.requestDisallowInterceptTouchEvent(true);
            scrollDirection = ScrollDirection.None;
            return false;
        }

        this.requestDisallowInterceptTouchEvent(false);
        return super.onTouchEvent(event);
        */
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void setTotalIndex(int totalIndex) {
        this.totalIndex = totalIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getTotalIndex() {
        return totalIndex;
    }


}
