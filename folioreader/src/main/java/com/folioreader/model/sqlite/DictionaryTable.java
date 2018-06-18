package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gautam chibde on 28/6/17.
 */

public class DictionaryTable {

    public static final String TABLE_NAME = "dictionary_table";

    public static final String ID = "_id";
    public static final String WORD = "word";
    public static final String MEANING = "meaning";
    private SQLiteDatabase database;

    public DictionaryTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + WORD + " TEXT" + ","
            + MEANING + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

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

    public String getMeaningForWord(String word) {
        Cursor c = database.rawQuery("SELECT * FROM "
                + TABLE_NAME +
                " WHERE " + WORD + " = \"" + word.trim() + "\"", null);
        if (c.moveToFirst()) {
            String toRetuen = c.getString(2);
            c.close();
            return toRetuen;
        }
        c.close();
        return null;
    }

    public List<String> getMeaning(String word) {
        List<String> words = new ArrayList<>();
        String meaning = getMeaningForWord(word);
        if (meaning != null) {
            words.add(meaning);
            return words;
        } else {
            return getProbableCombinations(word);
        }
    }

    private List<String> getProbableCombinations(String word) {
        List<String> combinations = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String m = getMeaningForWord(word.substring(0, word.length() - i));
            if (m != null) {
                combinations.add(m);
            }
        }
        return combinations;
    }
}
