package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {
    private static final String TAG = "DBAdapter";

    private Context mContext;
    public static SQLiteDatabase mDatabase;

    public DbAdapter(Context ctx) {
        this.mContext = ctx;
        mDatabase = FolioDatabaseHelper.getInstance(mContext).getMyWritableDatabase();
    }

    public static boolean insert(String table, ContentValues contentValues) {

        return mDatabase.insert(table, null, contentValues) > 0;
    }

    public static boolean update(String table, String key, String value, ContentValues contentValues) {

        return mDatabase.update(table, contentValues, key + "=?", new String[]{value}) > 0;
    }

    public static Cursor getHighLightsForBookId(String bookId) {
        return mDatabase.rawQuery("SELECT * FROM " + HighLightTable.TABLE_NAME + " WHERE " + HighLightTable.COL_BOOK_ID + " = '" + bookId + "'", null);
    }

    public static Cursor getAllHighlightRangy() {
        return mDatabase.rawQuery("SELECT * FROM " + HighLightRangyTable.TABLE_NAME, null);
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

    public static int isHighLightExist(String bookId) {
        Cursor c = mDatabase.rawQuery("SELECT " + HighLightRangyTable.ID + " FROM " + HighLightRangyTable.TABLE_NAME + " WHERE " + HighLightTable.COL_BOOK_ID + " = '" + bookId.trim() + "'", null);
        if (c.moveToFirst()) {
            int id = c.getInt(0);
            c.close();
            return id;
        } else {
            c.close();
            return -1;
        }
    }

    public static boolean saveHighLight(ContentValues highlightContentValues) {
        return mDatabase.insert(HighLightTable.TABLE_NAME, null, highlightContentValues) > 0;
    }

    public static boolean updateHighLight(ContentValues highlightContentValues, String id) {
        return mDatabase.update(HighLightTable.TABLE_NAME, highlightContentValues, HighLightTable.ID + " = " + id, null) > 0;
    }

    public static boolean saveHighLightRangy(ContentValues highlightContentValues) {
        return mDatabase.insert(HighLightRangyTable.TABLE_NAME, null, highlightContentValues) > 0;
    }

    public static boolean updateHighLightRangy(ContentValues highlightContentValues, String id) {
        return mDatabase.update(HighLightRangyTable.TABLE_NAME, highlightContentValues, HighLightTable.ID + " = " + id, null) > 0;

    }

    public static String getRangyForHref(String query) {
        Cursor c = mDatabase.rawQuery(query, null);
        if (c.moveToFirst()) {
            String rangy = c.getString(0);
            c.close();
            return rangy;
        } else {
            c.close();
            return "";
        }
    }

    public static Cursor getHighlightsForPageId(String query, String pageId) {
        return mDatabase.rawQuery(query, null);
    }
}
