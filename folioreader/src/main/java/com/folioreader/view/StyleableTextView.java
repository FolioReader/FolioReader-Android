package com.folioreader.view;

import com.folioreader.R;
import com.folioreader.util.UiUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class StyleableTextView extends TextView {

    public StyleableTextView(Context context) {
        super(context);
    }

    public StyleableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.StyleableTextView,
                R.styleable.StyleableTextView_font);
    }

    public StyleableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.StyleableTextView,
                R.styleable.StyleableTextView_font);
    }

}
