package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

/**
 * Created by gautam on 28/6/17.
 */

public class DictionaryTable {

    public static String TABLE_NAME = "highlight_table";

    public static String ID = "_id";
    public static String WORD = "word";
    public static String MEANING = "meaning";
    private SQLiteDatabase database;

    public DictionaryTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + WORD + " TEXT" + ","
            + MEANING + " TEXT" + ")";

    public static String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public boolean insertWord(String word, String meaning) {
        ContentValues values = new ContentValues();
        values.put(WORD, word);
        values.put(MEANING, meaning);
        return database.insert(TABLE_NAME, null, values) > 0;
    }

    public void insert(Map<String, String> map) {
        database.beginTransaction();
        for (String key : map.keySet()) {
            insertWord(key.toLowerCase(), map.get(key));
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public String getMeaning(String word) {
        Cursor c = database.rawQuery("SELECT * FROM "
                + TABLE_NAME +
                " WHERE " + WORD + " = '" + word.trim()+"'", null);
        if (c.moveToFirst()) {
            String toRetuen = c.getString(2);
            c.close();
            return toRetuen;
        }
        c.close();
        return null;
    }
}
