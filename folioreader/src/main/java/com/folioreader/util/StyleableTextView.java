package com.folioreader.util;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import com.folioreader.R;

public class StyleableTextView extends AppCompatTextView {

    public StyleableTextView(Context context, String font) {
        super(context);
        setCustomFont(context, font);
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

    private void setCustomFont(Context context, String font) {
        UiUtil.setCustomFont(this, context, font);
    }
}
