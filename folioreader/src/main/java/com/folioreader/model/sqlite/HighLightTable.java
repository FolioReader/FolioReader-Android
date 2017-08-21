package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.folioreader.model.Highlight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HighLightTable {
    public static String TABLE_NAME = "highlight_table";

    public static String ID = "_id";
    public static String COL_BOOK_ID = "bookId";
    private static String COL_CONTENT = "content";
    private static String COL_DATE = "date";
    private static String COL_TYPE = "type";
    private static String COL_WEB_VIEW_SCROLL = "webView_scroll";
    private static String COL_PAGE_NUMBER = "page_number";
    private static String COL_NOTE = "note";

    public static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_WEB_VIEW_SCROLL + " INTEGER" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_NOTE + " TEXT" + ")";

    public static String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static String TAG = HighLightTable.class.getSimpleName();

    public static ContentValues getHighlightContentValues(Highlight hightlight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, hightlight.getBookId());
        contentValues.put(COL_CONTENT, hightlight.getContent());
        contentValues.put(COL_DATE, getDateTimeString(hightlight.getDate()));
        contentValues.put(COL_TYPE, hightlight.getType());
        contentValues.put(COL_WEB_VIEW_SCROLL, hightlight.getScrollPosition());
        contentValues.put(COL_PAGE_NUMBER, hightlight.getPageNumber());
        contentValues.put(COL_NOTE, hightlight.getNote());
        return contentValues;
    }


    public static ArrayList<Highlight> getAllHighlights(String bookId) {
        ArrayList<Highlight> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getHighLightsForBookId(bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(new Highlight(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_WEB_VIEW_SCROLL)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE))));
        }
        return highlights;
    }

    public static void insertHighlight(Highlight highlight) {
        DbAdapter.saveHighLight(getHighlightContentValues(highlight));
    }

    //TODO
    public static void deleteHighlight(String highlightId) {
    }

    //TODO
    public static void updateHighlight(Highlight highlight) {
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1 = new Date();
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }
}



