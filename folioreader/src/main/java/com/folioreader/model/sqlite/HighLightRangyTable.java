package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by gautam on 21/8/17.
 */

public class HighLightRangyTable {
    public static String TABLE_NAME = "highlight_rangy_table";

    public static String ID = "_id";
    public static String COL_BOOK_ID = "bookId";
    public static String COL_PAGE_ID = "pageId";
    public static String COL_RANGY = "rangy";

    public static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ")";

    public static String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static ContentValues getHighlightContentValues(HighLightRangy highLightRangy) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, highLightRangy.getBookId());
        contentValues.put(COL_PAGE_ID, highLightRangy.getPageId());
        contentValues.put(COL_RANGY, highLightRangy.getRangy());
        return contentValues;
    }

    public static ArrayList<HighLightRangy> getAll(String bookId) {
        ArrayList<HighLightRangy> highlights = new ArrayList<>();
        Cursor highlightCursor = DbAdapter.getAllHighlightRangy();
        while (highlightCursor.moveToNext()) {
            highlights.add(new HighLightRangy(
                    highlightCursor.getInt(highlightCursor.getColumnIndex(ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_BOOK_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_PAGE_ID)),
                    highlightCursor.getString(highlightCursor.getColumnIndex(COL_RANGY))
            ));
        }
        return highlights;
    }

    public static void saveHighLight(HighLightRangy highlight) {
        int id = DbAdapter.isHighLightExist(highlight.getBookId());
        if (id == -1) {
            DbAdapter.saveHighLightRangy(getHighlightContentValues(highlight));
        } else {
            DbAdapter.updateHighLightRangy(getHighlightContentValues(highlight), String.valueOf(id));
        }
        Log.i("DATAbase", "highlight = " + getAll("dd"));
    }


    public static String getRangyForHref(String pageName) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = '" + pageName + "'";
        return DbAdapter.getRangyForHref(query);
    }
}
