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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StateSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import com.folioreader.AppContext;
import com.folioreader.R;
import com.folioreader.ui.view.UnderlinedTextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Hashtable;

/**
 * Created by mahavir on 3/30/16.
 */
public class UiUtil {

    private static final String LOG_TAG = UiUtil.class.getSimpleName();

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

    public static ColorStateList getColorList(@ColorInt int selectedColor,
                                              @ColorInt int unselectedColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_selected},
                new int[]{}
        };
        int[] colors = new int[]{
                selectedColor,
                unselectedColor
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
        if (type.equals("highlight_yellow")) {
            setUnderLineColor(textView, context, R.color.highlight_yellow, R.color.highlight_yellow);
        } else if (type.equals("highlight_green")) {
            setUnderLineColor(textView, context, R.color.highlight_green, R.color.highlight_green);
        } else if (type.equals("highlight_blue")) {
            setUnderLineColor(textView, context, R.color.highlight_blue, R.color.highlight_blue);
        } else if (type.equals("highlight_pink")) {
            setUnderLineColor(textView, context, R.color.highlight_pink, R.color.highlight_pink);
        } else if (type.equals("highlight_underline")) {
            setUnderLineColor(textView, context, android.R.color.transparent, android.R.color.holo_red_dark);
            textView.setUnderlineWidth(2.0f);
        }
    }


    private static void setUnderLineColor(UnderlinedTextView underlinedTextView, Context context, int background, int underlinecolor) {
        underlinedTextView.setBackgroundColor(ContextCompat.getColor(context,
                background));
        underlinedTextView.setUnderLineColor(ContextCompat.getColor(context,
                underlinecolor));
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
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

    public static void setColorIntToDrawable(@ColorInt int color, Drawable drawable) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setColorResToDrawable(@ColorRes int colorResId, Drawable drawable) {
        try {
            int color = ContextCompat.getColor(AppContext.get(), colorResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "-> Exception in setColorResToDrawable -> ", e);
        }
    }

    public static void setEditTextCursorColor(EditText editText, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(editText);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(editText.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            if (Build.VERSION.SDK_INT == 15) {
                // Get the editor
                Class<?> drawableFieldClass = TextView.class;
                // Set the drawables
                field = drawableFieldClass.getDeclaredField("mCursorDrawable");
                field.setAccessible(true);
                field.set(editText, drawables);

            } else if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 27) {
                // Get the editor
                field = TextView.class.getDeclaredField("mEditor");
                field.setAccessible(true);
                Object editor = field.get(editText);
                // Set the drawables
                field = editor.getClass().getDeclaredField("mCursorDrawable");
                field.setAccessible(true);
                field.set(editor, drawables);

            } else if (Build.VERSION.SDK_INT >= 28) {
                // TODO -> Not working for 28
                // Get the editor
                field = TextView.class.getDeclaredField("mEditor");
                field.setAccessible(true);
                Object editor = field.get(editText);
                // Set the drawables
                field = editor.getClass().getDeclaredField("mDrawableForCursor");
                field.setAccessible(true);
                field.set(editor, drawables[0]);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "-> ", e);
        }
    }

    public static void setEditTextHandleColor(EditText editText, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field fieldLeftRes = TextView.class.getDeclaredField("mTextSelectHandleLeftRes");
            fieldLeftRes.setAccessible(true);
            int leftDrawableResId = fieldLeftRes.getInt(editText);

            Field fieldRightRes = TextView.class.getDeclaredField("mTextSelectHandleRightRes");
            fieldRightRes.setAccessible(true);
            int rightDrawableResId = fieldRightRes.getInt(editText);

            Field fieldCenterRes = TextView.class.getDeclaredField("mTextSelectHandleRes");
            fieldCenterRes.setAccessible(true);
            int centerDrawableResId = fieldCenterRes.getInt(editText);

            // Get the drawable and set a color filter
            Drawable drawableLeft = ContextCompat.getDrawable(editText.getContext(), leftDrawableResId);
            drawableLeft.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable drawableRight = ContextCompat.getDrawable(editText.getContext(), rightDrawableResId);
            drawableRight.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable drawableCenter = ContextCompat.getDrawable(editText.getContext(), centerDrawableResId);
            drawableCenter.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            if (Build.VERSION.SDK_INT == 15) {
                // Get the editor
                Class<?> drawableFieldClass = TextView.class;

                // Set the drawables
                Field fieldLeft = drawableFieldClass.getDeclaredField("mSelectHandleLeft");
                fieldLeft.setAccessible(true);
                fieldLeft.set(editText, drawableLeft);

                Field fieldRight = drawableFieldClass.getDeclaredField("mSelectHandleRight");
                fieldRight.setAccessible(true);
                fieldRight.set(editText, drawableRight);

                Field fieldCenter = drawableFieldClass.getDeclaredField("mSelectHandleCenter");
                fieldCenter.setAccessible(true);
                fieldCenter.set(editText, drawableCenter);

            } else {
                // Get the editor
                Field fieldEditor = TextView.class.getDeclaredField("mEditor");
                fieldEditor.setAccessible(true);
                Object editor = fieldEditor.get(editText);

                // Set the drawables
                Field fieldLeft = editor.getClass().getDeclaredField("mSelectHandleLeft");
                fieldLeft.setAccessible(true);
                fieldLeft.set(editor, drawableLeft);

                Field fieldRight = editor.getClass().getDeclaredField("mSelectHandleRight");
                fieldRight.setAccessible(true);
                fieldRight.set(editor, drawableRight);

                Field fieldCenter = editor.getClass().getDeclaredField("mSelectHandleCenter");
                fieldCenter.setAccessible(true);
                fieldCenter.set(editor, drawableCenter);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "-> ", e);
        }
    }

    public static StateListDrawable createStateDrawable(@ColorInt int colorSelected,
                                                        @ColorInt int colorNormal) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(colorSelected));
        stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(colorNormal));
        return stateListDrawable;
    }

    public static GradientDrawable getShapeDrawable(@ColorInt int color) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setStroke(pxToDp(2), color);
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(pxToDp(3));
        return gradientDrawable;
    }

    public static void setShapeColor(View view, @ColorInt int color) {
        ((GradientDrawable) view.getBackground()).setColor(color);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static void setStatusBarColor(Window window, @ColorInt int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public static String rectToDOMRectJson(Rect rect) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", rect.left);
            jsonObject.put("y", rect.top);
            jsonObject.put("width", rect.width());
            jsonObject.put("height", rect.height());
            return jsonObject.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "-> ", e);
        }
        return null;
    }
}
