package com.folioreader.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by PC on 6/9/2016.
 */
public class SharedPreferenceUtil {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public static void putSharedPreferencesInt(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void putSharedPreferencesBoolean(Context context, String key, boolean val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, val);
        edit.commit();
    }

    public static void putSharedPreferencesString(Context context, String key, String val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, val);
        edit.commit();
    }

    public static void putSharedPreferencesStringSet(Context context, String key, Set<String> val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(key, val);
        editor.commit();
    }

    public static void putSharedPreferencesFloat(Context context, String key, float val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putFloat(key, val);
        edit.commit();
    }

    public static void putSharedPreferencesLong(Context context, String key, long val) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(key, val);
        edit.commit();
    }

    public static long getSharedPreferencesLong(Context context, String key, long defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(key, defaultValue);
    }

    public static float getSharedPreferencesFloat(Context context, String key, float defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(key, defaultValue);
    }

    public static String getSharedPreferencesString(Context context, String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    public static Set<String> getSharedPreferencesStringSet(Context context, String key, Set<String> defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(key, defaultValue);
    }

    public static int getSharedPreferencesInt(Context context, String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    public static boolean getSharedPreferencesBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, defaultValue);
    }

    public static boolean removeSharedPreferencesKey(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        return editor.commit();
    }
}
