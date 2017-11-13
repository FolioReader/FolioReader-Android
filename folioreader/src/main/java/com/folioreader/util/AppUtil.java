package com.folioreader.util;

import android.content.Context;
import android.util.Log;

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

import static com.folioreader.Constants.BOOK_STATE;
import static com.folioreader.Constants.BOOK_TITLE;
import static com.folioreader.Constants.VIEWPAGER_POSITION;
import static com.folioreader.Constants.WEBVIEW_SCROLL_POSITION;
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
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = null;
            value = jObject.get(key);

            if(value instanceof JSONObject) {
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

    public static void saveBookState(Context context, String bookTitle, int folioPageViewPagerPosition, int webViewScrollPosition) {
        SharedPreferenceUtil.removeSharedPreferencesKey(context, bookTitle + BOOK_STATE);
        JSONObject obj = new JSONObject();
        try {
            obj.put(BOOK_TITLE, bookTitle);
            obj.put(WEBVIEW_SCROLL_POSITION, webViewScrollPosition);
            obj.put(VIEWPAGER_POSITION, folioPageViewPagerPosition);
            SharedPreferenceUtil.
                    putSharedPreferencesString(
                            context, bookTitle + BOOK_STATE, obj.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static boolean checkPreviousBookStateExist(Context context, String bookName) {
        String json
                = getSharedPreferencesString(
                context, bookName + BOOK_STATE,
                null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                String bookTitle = jsonObject.getString(BOOK_TITLE);
                if (bookTitle.equals(bookName))
                    return true;
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static int getPreviousBookStatePosition(Context context, String bookName) {
        String json
                = getSharedPreferencesString(context,
                bookName + BOOK_STATE,
                null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getInt(VIEWPAGER_POSITION);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return 0;
            }
        }
        return 0;
    }

    public static int getPreviousBookStateWebViewPosition(Context context, String bookTitle) {
        String json = getSharedPreferencesString(context, bookTitle + BOOK_STATE, null);
        if (json != null) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getInt(WEBVIEW_SCROLL_POSITION);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                return 0;
            }
        }
        return 0;
    }


    public static void saveConfig(Context context, Config config) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(Config.CONFIG_FONT, config.getFont());
            obj.put(Config.CONFIG_FONT_SIZE, config.getFontSize());
            obj.put(Config.CONFIG_IS_NIGHTMODE, config.isNightMode());
            obj.put(Config.CONFIG_IS_THEMECOLOR, config.getThemeColor());
            obj.put(Config.CONFIG_IS_TTS,config.isShowTts());
            SharedPreferenceUtil.
                    putSharedPreferencesString(
                            context, Config.INTENT_CONFIG, obj.toString());
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
}







