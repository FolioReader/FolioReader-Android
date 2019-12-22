package com.folioreader.model.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VietnameseDictionaryDatabaseHelper {

    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase vi_DictionaryDB;
    private static VietnameseDictionaryDatabaseHelper instance;

    Cursor cursor = null;

    private VietnameseDictionaryDatabaseHelper(Context context) {
        this.openHelper = new SQLiteExternalDatabase_VietnameseDictionaryHelper(context);
    }

    public static VietnameseDictionaryDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VietnameseDictionaryDatabaseHelper(context);
            Log.e("DBHelper: ", "DB null");
        }
        return instance;
    }


    public void openDatabase() {
        this.vi_DictionaryDB = openHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        if (this.vi_DictionaryDB != null) {
            this.vi_DictionaryDB.close();
        }
    }

    public String getDefine(String word) {
        cursor = vi_DictionaryDB.rawQuery("SELECT DEFINE FROM VIETNAMESE_DICTIONARY WHERE WORD = '" + word+"' ", new String[]{});
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            String define = cursor.getString(0);
            buffer.append(" " + define);
        }
        return buffer.toString();
    }

//    public void QueryData(String query)
//    {
//        SQLiteDatabase database = getWritableDatabase();
//        database.execSQL(query);
//    }

//    public Cursor GetData(String query)
//    {
//        SQLiteDatabase database = getReadableDatabase();
//        return database.rawQuery(query, null);
//    }
}
