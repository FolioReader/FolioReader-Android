package com.folioreader.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static String FOLIO_READER_ROOT = "folioreader";

    private enum FileType {
        OPS,
        OEBPS,
        NONE
    }

    public static Map<String, String> stringToJsonMap(String string) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            JSONObject jObject = jsonArray.getJSONObject(0);
            Iterator<?> keys = jObject.keys();

            keys.hasNext();
            String key = (String) keys.next();
            String value = jObject.getString(key);
            map.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String formatDate(Date hightlightDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault());
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
}







