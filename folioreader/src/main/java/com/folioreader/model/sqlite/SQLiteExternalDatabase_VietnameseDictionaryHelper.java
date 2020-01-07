package com.folioreader.model.sqlite;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

//VietnamseDictionaryDatabase contain a table named VIETNAMESE_DICTIONARY which containt 2 columns:
//WORD (Primary Key): w_word
//DEFINE: wdf<1>_define<1> wex<1>_example<1> wdf<2>_define<2> wex<2>_example<2>

public class SQLiteExternalDatabase_VietnameseDictionaryHelper extends SQLiteAssetHelper {

    private static final String DB_NAME = "VietnameseDictionary.db.sqbpro";
    private static final int DB_VERSION = 1;

    public SQLiteExternalDatabase_VietnameseDictionaryHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
