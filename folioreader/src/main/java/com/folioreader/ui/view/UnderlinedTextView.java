package com.folioreader.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import com.folioreader.R;

/**
 * Created by mobisys on 7/4/2016.
 */
public class UnderlinedTextView extends AppCompatTextView {

    private Rect mRect;
    private Paint mPaint;
    private int mColor;
    private float mDensity;
    private float mStrokeWidth;

    public UnderlinedTextView(Context context) {
        this(context, null, 0);
    }

    public UnderlinedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnderlinedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attributeSet, int defStyle) {

        mDensity = context.getResources().getDisplayMetrics().density;

        TypedArray typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.UnderlinedTextView,
                        defStyle, 0);
        mStrokeWidth =
                typedArray.getDimension(
                        R.styleable.UnderlinedTextView_underlineWidth,
                        mDensity * 2);
        typedArray.recycle();

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor); //line mColor
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    public int getUnderLineColor() {
        return mColor;
    }

    public void setUnderLineColor(int mColor) {
        this.mColor = mColor;
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor); //line mColor
        mPaint.setStrokeWidth(mStrokeWidth);
        postInvalidate();
    }

    public float getUnderlineWidth() {
        return mStrokeWidth;
    }

    public void setUnderlineWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();

        final Layout layout = getLayout();
        float xStart, xStop, xDiff;
        int firstCharInLine, lastCharInLine;

        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, mRect);
            firstCharInLine = layout.getLineStart(i);
            lastCharInLine = layout.getLineEnd(i);

            xStart = layout.getPrimaryHorizontal(firstCharInLine);
            xDiff = layout.getPrimaryHorizontal(firstCharInLine + 1) - xStart;
            xStop = layout.getPrimaryHorizontal(lastCharInLine - 1) + xDiff;

            canvas.drawLine(xStart,
                    baseline + mStrokeWidth,
                    xStop,
                    baseline + mStrokeWidth,
                    mPaint);
        }

        super.onDraw(canvas);
    }
}