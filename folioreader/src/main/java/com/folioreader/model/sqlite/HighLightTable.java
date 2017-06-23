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
    public static String COL_CONTENT = "content";
    public static String COL_CONTENT_POST = "contentPost";
    public static String COL_CONTENT_PRE = "contentPre";
    public static String COL_DATE = "date";
    public static String COL_HIGHLIGHT_ID = "highlightId";
    public static String COL_PAGE = "page";
    public static String COL_TYPE = "type";
    public static String COL_PAGER_POSITION = "currentPagerPostion";
    public static String COL_CURRENT_WEBVIEWSCROLL = "currentWebviewScrollPos";
    public static String COL_NOTE = "note";

    public static String[] ALL_COLUMNS = new String[]{ID, COL_BOOK_ID, COL_CONTENT, COL_CONTENT_POST,
            COL_CONTENT_PRE, COL_DATE, COL_HIGHLIGHT_ID, COL_PAGE, COL_TYPE, COL_PAGER_POSITION, COL_CURRENT_WEBVIEWSCROLL, COL_NOTE};

    public static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_CONTENT_POST + " TEXT" + ","
            + COL_CONTENT_PRE + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_HIGHLIGHT_ID + " TEXT" + ","
            + COL_PAGE + " INTEGER" + ","
            + COL_TYPE + " TEXT" + ","
            + COL_PAGER_POSITION + " INTEGER" + ","
            + COL_CURRENT_WEBVIEWSCROLL + " INTEGER" + ","
            + COL_NOTE + " TEXT" + ")";

    public static String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static String WHERE_ID_EQUALS = ID + "=?";
    public static String WHERE_SERVER_ID_EQUALS = COL_BOOK_ID + "=?";


    public static String TAG = HighLightTable.class.getSimpleName();

    public static ContentValues getHighlightContentValues(Highlight hightlight) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, hightlight.getBookId());
        contentValues.put(COL_CONTENT, hightlight.getContent());
        contentValues.put(COL_CONTENT_POST, hightlight.getContentPost());
        contentValues.put(COL_CONTENT_PRE, hightlight.getContentPre());
        contentValues.put(COL_DATE, getDateTimeString(hightlight.getDate()));
        contentValues.put(COL_HIGHLIGHT_ID, hightlight.getHighlightId());
        contentValues.put(COL_PAGE, hightlight.getPage());
        contentValues.put(COL_TYPE, hightlight.getType());
        contentValues.put(COL_PAGER_POSITION, hightlight.getCurrentPagerPostion());
        contentValues.put(COL_CURRENT_WEBVIEWSCROLL, hightlight.getCurrentWebviewScrollPos());
        contentValues.put(COL_NOTE, hightlight.getNote());
        return contentValues;
    }


    public static ArrayList<Highlight> getAllHighlights(String bookId) {
        ArrayList<Highlight> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getAllByKey(HighLightTable.TABLE_NAME, HighLightTable.ALL_COLUMNS, HighLightTable.COL_BOOK_ID, bookId);
        while (highlightCursor.moveToNext()) {
            highlights.add(new Highlight(highlightCursor.getString(1),
                    highlightCursor.getString(2),
                    highlightCursor.getString(3),
                    highlightCursor.getString(4),
                    getDateTime(highlightCursor.getString(5)),
                    highlightCursor.getString(6),
                    highlightCursor.getInt(7),
                    highlightCursor.getString(8),
                    highlightCursor.getInt(9),
                    highlightCursor.getInt(10),
                    highlightCursor.getString(11)));
        }


        return highlights;
    }

    public static void deleteHighlight(String highlightId) {
        if (DbAdapter.deleteById(HighLightTable.TABLE_NAME, HighLightTable.COL_HIGHLIGHT_ID, highlightId)) {
            Log.d(TAG, "highlight deleted sucessfully");
        } else {
            Log.d(TAG, "error while highlight deleting");
        }
    }

    public static void insertHighlight(Highlight highlight) {
        if (DbAdapter.insert(HighLightTable.TABLE_NAME, getHighlightContentValues(highlight))) {
            Log.d(TAG, "highlight inserted sucessfully");
        } else {
            Log.d(TAG, "error while highlight inserting");
        }
    }

    public static void updateHighlight(Highlight highlight) {
        if (DbAdapter.update(HighLightTable.TABLE_NAME, COL_HIGHLIGHT_ID, highlight.getHighlightId(), getHighlightContentValues(highlight))) {
            Log.d(TAG, "highlight updated sucessfully");
        } else {
            Log.d(TAG, "error while highlight updateing");
        }
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

    public static void updateHighlightStyle(String id,String style) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL_TYPE,style);
        if (DbAdapter.update(HighLightTable.TABLE_NAME, COL_HIGHLIGHT_ID, id, contentValues)) {
            Log.d(TAG, "highlight updated sucessfully");
        } else {
            Log.d(TAG, "error while highlight updateing");
        }
    }
}



