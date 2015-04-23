package br.com.rsa.folioreader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import br.com.rsa.folioreader.entities.FingerPairDirection;
import br.com.rsa.folioreader.enummerators.ScrollDirection;

/**
 * Created by rodrigo.almeida on 10/04/15.
 */
public class FolioReaderWebView extends WebView {

    private int totalIndex;
    private int currentIndex;
    private float downYValue = 0;
    private float downXValue = 0;
    private FingerPairDirection fingerPairDirection;

    public FolioReaderWebView(Context context) {
        super(context);
        init(context);
    }

    public FolioReaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.getSettings().setJavaScriptEnabled(true);
        this.setWebViewClient(new WebViewClient() {
        });
        fingerPairDirection = new FingerPairDirection();
        setVerticalScrollbarOverlay(true);
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
    public int getContentHeight() {
        return this.computeVerticalScrollRange() - this.getHeight();
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public void setTotalIndex(int totalIndex) {
        this.totalIndex = totalIndex;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

    }
}
