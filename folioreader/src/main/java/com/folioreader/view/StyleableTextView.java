package com.folioreader.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.folioreader.R;
import com.folioreader.util.UiUtil;

public class StyleableTextView extends AppCompatTextView {

    public StyleableTextView(Context context) {
        super(context);
    }

    public StyleableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.StyleableTextView,
                R.styleable.StyleableTextView_folio_font);
    }

    public StyleableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.StyleableTextView,
                R.styleable.StyleableTextView_folio_font);
    }

}
