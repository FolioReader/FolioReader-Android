package com.folioreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.folioreader.model.Highlight;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by mobisys2 on 5/21/2016.
 */

public class FolioReaderDB extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "folioReader.db";

    private static FolioReaderDB mInstance = null;

    private Dao<Highlight, Integer> mHighlightTableDao;

    public FolioReaderDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static FolioReaderDB getInstance(Context context) {
        if (mInstance == null) {
            mInstance = OpenHelperManager.getHelper(context, FolioReaderDB.class);
          }
        return mInstance;
    }

    public Dao<Highlight, Integer> getHighlightDao() throws SQLException {
        if (mHighlightTableDao == null) {
            mHighlightTableDao = getDao(Highlight.class);
        }
        return mHighlightTableDao;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Highlight.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(FolioReaderDB.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Highlight.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(FolioReaderDB.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }
}
