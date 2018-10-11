package com.folioreader.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.folioreader.Config;
import com.folioreader.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static com.folioreader.util.SharedPreferenceUtil.getSharedPreferencesString;

/**
 * Created by mahavir on 5/7/16.
 */
public class AppUtil {

    private static final String SMIL_ELEMENTS = "smil_elements";
    private static final String TAG = AppUtil.class.getSimpleName();
    private static final String FOLIO_READER_ROOT = "folioreader";

    private enum FileType {
        OPS,
        OEBPS,
        NONE
    }

    public static Map<String, String> toMap(String jsonString) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            JSONObject jObject = jsonArray.getJSONObject(0);
            Iterator<String> keysItr = jObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Object value = null;
                value = jObject.get(key);

                if (value instanceof JSONObject) {
                    value = toMap(value.toString());
                }
                map.put(key, value.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "toMap failed", e);
        }
        return map;
    }

    public static String charsetNameForURLConnection(URLConnection connection) {
        // see https://stackoverflow.com/a/3934280/1027646
        String contentType = connection.getContentType();
        String[] values = contentType.split(";");
        String charset = null;

        for (String value : values) {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset=")) {
                charset = value.substring("charset=".length());
                break;
            }
        }

        if (charset == null || charset.isEmpty()) {
            charset = "UTF-8"; //Assumption
        }

        return charset;
    }

    public static String formatDate(Date hightlightDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
        return simpleDateFormat.format(hightlightDate);
    }

    public static void saveConfig(Context context, Config config) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(Config.CONFIG_FONT, config.getFont());
            obj.put(Config.CONFIG_FONT_SIZE, config.getFontSize());
            obj.put(Config.CONFIG_IS_NIGHT_MODE, config.isNightMode());
            obj.put(Config.CONFIG_THEME_COLOR_INT, config.getThemeColor());
            obj.put(Config.CONFIG_IS_TTS, config.isShowTts());
            obj.put(Config.CONFIG_ALLOWED_DIRECTION, config.getAllowedDirection().toString());
            obj.put(Config.CONFIG_DIRECTION, config.getDirection().toString());
            SharedPreferenceUtil.putSharedPreferencesString(context, Config.INTENT_CONFIG,
                    obj.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static Config getSavedConfig(Context context) {
        String json = getSharedPreferencesString(context, Config.INTENT_CONFIG, null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return new Config(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "ACTION_HOVER_EXIT";
        }

        if (Build.VERSION.SDK_INT >= 23) {
            switch (action) {
                case MotionEvent.ACTION_BUTTON_PRESS:
                    return "ACTION_BUTTON_PRESS";
                case MotionEvent.ACTION_BUTTON_RELEASE:
                    return "ACTION_BUTTON_RELEASE";
            }
        }

        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager imm = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token
        if (view == null)
            view = new View(activity);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }
}







