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

    public static boolean insert(String table, ContentValues contentValues) {

        return mDatabase.insert(table, null, contentValues) > 0;
    }

    public static boolean update(String table, String key, String value, ContentValues contentValues) {

        return mDatabase.update(table, contentValues, key + "=?", new String[]{value}) > 0;
    }

    public static Cursor getHighLightsForBookId(String bookId) {
        return mDatabase.rawQuery("SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.COL_BOOK_ID + " = \"" + bookId + "\"", null);
    }

    public boolean deleteAll(String table) {
        return mDatabase.delete(table, null, null) > 0;
    }

    public boolean deleteAll(String table, String whereClause, String[] whereArgs) {
        return mDatabase.delete(table, whereClause + "=?", whereArgs) > 0;
    }

    public Cursor getAll(String table, String[] projection, String selection,
                         String[] selectionArgs, String orderBy) {
        return mDatabase.query(table, projection, selection, selectionArgs, null, null, orderBy);
    }

    public Cursor getAll(String table) {
        return getAll(table, null, null, null, null);
    }

    public Cursor get(String table, long id, String[] projection, String key) throws SQLException {
        return mDatabase.query(table, projection,
                key + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
    }

    public static Cursor getAllByKey(String table, String[] projection, String key, String value) throws SQLException {
        return mDatabase.query(table, projection,
                key + "=?", new String[]{value}, null, null, null, null);
    }

    public Cursor get(String table, long id) throws SQLException {
        return get(table, id, null, FolioDatabaseHelper.KEY_ID);
    }

    public static boolean deleteById(String table, String key, String value) {
        return mDatabase.delete(table, key + "=?", new String[]{value}) > 0;
    }

    public Cursor getMaxId(String tableName, String key) {
        return mDatabase.rawQuery("SELECT MAX(" + key + ") FROM " + tableName, null);
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
