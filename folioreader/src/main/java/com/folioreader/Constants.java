package com.folioreader;

import android.Manifest;

/**
 * Created by mobisys on 10/4/2016.
 */
public class Constants {
    public static final String SELECTED_CHAPTER_POSITION = "selected_chapter_position";
    public static final String TYPE = "type";
    public static final String CHAPTER_SELECTED = "chapter_selected";
    public static final String HIGHLIGHT_SELECTED = "highlight_selected";
    public static final String BOOK_TITLE = "book_title";
    public static final int PORT_NUMBER = 8080;
    public static final String LOCALHOST = "http://127.0.0.1:" + PORT_NUMBER + "/";
    public static final String SELECTED_WORD = "selected_word";
    public static final String DICTIONARY_BASE_URL = "http://api.pearson.com/v2/dictionaries/entries?headword=";
    public static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php?action=opensearch&namespace=0&format=json&search=";
    public static final int FONT_ANDADA = 1;
    public static final int FONT_LATO = 2;
    public static final int FONT_LORA = 3;
    public static final int FONT_RALEWAY = 4;
    public static final String DATE_FORMAT = "MMM dd, yyyy | HH:mm";
    public static final String ASSET = "file:///android_asset/";
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST = 102;

    public static String[] getWriteExternalStoragePerms() {
        return new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}
