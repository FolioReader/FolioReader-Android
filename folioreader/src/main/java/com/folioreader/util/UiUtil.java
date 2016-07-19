package com.folioreader.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

/**
 * Created by mahavir on 3/30/16.
 */
public class UiUtil {
    public static void setCustomFont(View view, Context ctx, AttributeSet attrs,
                                     int[] attributeSet, int fontId) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, attributeSet);
        String customFont = a.getString(fontId);
        setCustomFont(view, ctx, customFont);
        a.recycle();
    }

    private static boolean setCustomFont(View view, Context ctx, String asset) {
        if (TextUtils.isEmpty(asset))
            return false;
        Typeface tf = null;
        try {
            tf = getFont(ctx, asset);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(tf);
            } else {
                ((Button) view).setTypeface(tf);
            }
        } catch (Exception e) {
            Log.e("AppUtil", "Could not get typface  " + asset);
            return false;
        }

        return true;
    }

    private static final Hashtable<String, SoftReference<Typeface>> fontCache = new Hashtable<String, SoftReference<Typeface>>();

    public static Typeface getFont(Context c, String name) {
        synchronized (fontCache) {
            if (fontCache.get(name) != null) {
                SoftReference<Typeface> ref = fontCache.get(name);
                if (ref.get() != null) {
                    return ref.get();
                }
            }

            Typeface typeface = Typeface.createFromAsset(c.getAssets(), name);
            fontCache.put(name, new SoftReference<Typeface>(typeface));

            return typeface;
        }
    }
}
