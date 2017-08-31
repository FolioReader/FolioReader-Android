package com.folioreader.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.StateSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.folioreader.R;
import com.folioreader.view.UnderlinedTextView;

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

    public static boolean setCustomFont(View view, Context ctx, String asset) {
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

    public static ColorStateList getColorList(Context context, int selectedColor, int unselectedColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(context, selectedColor),
                ContextCompat.getColor(context, unselectedColor)
        };
        ColorStateList list = new ColorStateList(states, colors);
        return list;
    }

    public static void keepScreenAwake(boolean enable, Context context) {
        if (enable) {
            ((Activity) context)
                    .getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ((Activity) context)
                    .getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static void setBackColorToTextView(UnderlinedTextView textView, String type) {
        Context context = textView.getContext();
        if (type.equals("yellow")) {
            setUnderLineColor(textView, context, R.color.yellow, R.color.yellow);
        } else if (type.equals("green")) {
            setUnderLineColor(textView, context, R.color.green, R.color.green);
        } else if (type.equals("blue")) {
            setUnderLineColor(textView, context, R.color.blue, R.color.blue);
        } else if (type.equals("pink")) {
            setUnderLineColor(textView, context, R.color.pink, R.color.pink);
        } else if (type.equals("underline")) {
            setUnderLineColor(textView, context, android.R.color.transparent, android.R.color.holo_red_dark);
            textView.setUnderlineWidth(2.0f);
        }
    }


    private static void setUnderLineColor(UnderlinedTextView underlinedTextView, Context context, int background,int underlinecolor) {
        underlinedTextView.setBackgroundColor(ContextCompat.getColor(context,
                background));
        underlinedTextView.setUnderLineColor(ContextCompat.getColor(context,
                underlinecolor));
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copy", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void share(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sendIntent,
                context.getResources().getText(R.string.send_to)));
    }


    public static void setColorToImage(Context context, int color, Drawable drawable) {
        drawable.setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_ATOP);
    }


    public static StateListDrawable convertColorIntoStateDrawable(Context context, int colorSelected, int colorNormal) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(ContextCompat.getColor(context, colorSelected)));
        stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(ContextCompat.getColor(context, colorNormal)));
        return stateListDrawable;
    }

    public static GradientDrawable getShapeDrawable(Context context, int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setStroke(pxToDp(2), ContextCompat.getColor(context, color));
        gradientDrawable.setColor(ContextCompat.getColor(context, color));
        gradientDrawable.setCornerRadius(pxToDp(3));
        return gradientDrawable;
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
