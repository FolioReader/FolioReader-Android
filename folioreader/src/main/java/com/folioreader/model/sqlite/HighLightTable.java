package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.model.Highlight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HighLightTable {
    public static String TABLE_NAME = "highlight_table";

    public static String ID = "_id";
    public static String COL_BOOK_ID = "bookId";
    private static String COL_CONTENT = "content";
    private static String COL_DATE = "date";
    private static String COL_TYPE = "type";
    private static String COL_PAGE_NUMBER = "page_number";
    private static String COL_PAGE_ID = "pageId";
    private static String COL_RANGY = "rangy";
    private static String COL_NOTE = "note";

    public static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ","
            + COL_NOTE + " TEXT" + ")";

    public static String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static String TAG = HighLightTable.class.getSimpleName();

    public static ContentValues getHighlightContentValues(Highlight hightlight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, hightlight.getBookId());
        contentValues.put(COL_CONTENT, hightlight.getContent());
        contentValues.put(COL_DATE, getDateTimeString(hightlight.getDate()));
        contentValues.put(COL_TYPE, hightlight.getType());
        contentValues.put(COL_PAGE_NUMBER, hightlight.getPageNumber());
        contentValues.put(COL_PAGE_ID, hightlight.getPageId());
        contentValues.put(COL_RANGY, hightlight.getRangy());
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
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE))));
        }
        return highlights;
    }

    public static Highlight getHighlightId(int id) {
        Cursor highlightCursor = DbAdapter.getHighlightsForId(id);
        Highlight highLight = new Highlight();
        while (highlightCursor.moveToNext()) {
            highLight = new Highlight(highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(highlightCursor.getString(highlightCursor.getColumnIndex(COL_DATE))),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_TYPE)),
                    highlightCursor.getInt(highlightCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_NOTE)));
        }
        return highLight;
    }

    public static long insertHighlight(Highlight highlight) {
        return DbAdapter.saveHighLight(getHighlightContentValues(highlight));
    }

    public static boolean deleteHighlight(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = '" + rangy + "'";
        int id = DbAdapter.getIdForRangy(query);
        return id != -1 && deleteHighlight(id);
    }

    public static boolean deleteHighlight(int highlightId) {
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(highlightId));
    }

    public static List<String> getHighlightsForPageId(String pageId) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = '" + pageId + "'";
        Cursor c = DbAdapter.getHighlightsForPageId(query, pageId);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }

    public static boolean updateHighlight(Highlight highlight) {
        return DbAdapter.updateHighLight(getHighlightContentValues(highlight), String.valueOf(highlight.getId()));
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        Date date1 = new Date();
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static Highlight updateHighlightStyle(String rangy, String style) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = '" + rangy + "'";
        int id = DbAdapter.getIdForRangy(query);
        if (id != -1) {
            if (update(id, updateRangy(rangy, style), style.replace("highlight_", ""))) {
                return getHighlightId(id);
            }
        }
        return null;
    }

    public static Highlight getHighlightForRangy(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = '" + rangy + "'";
        return getHighlightId(DbAdapter.getIdForRangy(query));
    }

    private static String updateRangy(String rangy, String style) {
        /*Pattern p = Pattern.compile("\\highlight_\\w+");
        Matcher m = p.matcher(rangy);
        return m.replaceAll(style);*/
        String[] s = rangy.split("\\$");
        StringBuilder builder = new StringBuilder();
        for (String p : s) {
            if (TextUtils.isDigitsOnly(p)) {
                builder.append(p);
                builder.append("$");
            } else {
                builder.append(style);
                builder.append("$");
            }
        }
        return builder.toString();
    }

    private static boolean update(int id, String s, String color) {
        Highlight highlight = getHighlightId(id);
        highlight.setRangy(s);
        highlight.setType(color);
        return DbAdapter.updateHighLight(getHighlightContentValues(highlight), String.valueOf(id));
    }
}



