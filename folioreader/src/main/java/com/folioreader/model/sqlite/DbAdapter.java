package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {
    private static final String TAG = "DBAdapter";

    public static SQLiteDatabase mDatabase;

    public static void initialize(Context mContext) {
        mDatabase = FolioDatabaseHelper.getInstance(mContext).getMyWritableDatabase();
    }

    public static void terminate() {
        FolioDatabaseHelper.clearInstance();
    }

    public static Cursor getHighLightsForBookId(String bookId) {
        return mDatabase.rawQuery("SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.COL_BOOK_ID + " = \"" + bookId + "\"", null);
    }

    public static boolean deleteById(String table, String key, String value) {
        return mDatabase.delete(table, key + "=?", new String[]{value}) > 0;
    }

    public static long saveHighLight(ContentValues highlightContentValues) {
        return mDatabase.insert(HighLightTable.TABLE_NAME, null, highlightContentValues);
    }

    public static boolean updateHighLight(ContentValues highlightContentValues, String id) {
        return mDatabase.update(HighLightTable.TABLE_NAME, highlightContentValues, HighLightTable.ID + " = " + id, null) > 0;
    }

    public static Cursor getHighlightsForPageId(String query, String pageId) {
        return mDatabase.rawQuery(query, null);
    }

    public static int getIdForQuery(String query) {
        Cursor c = mDatabase.rawQuery(query, null);

        int id = -1;
        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex(HighLightTable.ID));
        }
        c.close();
        return id;
    }

    public static Cursor getHighlightsForId(int id) {
        return mDatabase.rawQuery("SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.ID + " = \"" + id + "\"", null);
    }
}
