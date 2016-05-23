package com.folioreader.database;

import android.content.Context;
import android.util.Log;

import com.folioreader.model.Highlight;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mobisys2 on 5/21/2016.
 */
public class HighlightTable {
    private static final String Tag = "HighlightTable";

    public static int createEntryInTable(Context context, Highlight highlight) {
        int status = -1;

        try {
            status = FolioReaderDB.getInstance(context).getHighlightDao().create(highlight);
        } catch (SQLException e) {
            Log.d(Tag + "CREAT", e.getMessage());
        }

        return status;
    }

    public static int createEntryInTableIfNotExist(Context context, Highlight highlight) {
        int status = -1;
        try {
            if (!isHighlightExistInDB(context, highlight))
                status = FolioReaderDB.getInstance(context).getHighlightDao().create(highlight);
        } catch (SQLException e) {
            Log.d(Tag + "CREAT", e.getMessage());
        }

        return status;
    }

    public static int updateHighlight(Context context, Highlight highlight) {
        try {
            int count = FolioReaderDB.getInstance(context).getHighlightDao().update(highlight);
            Log.d("HighlightTable", "Updated " + count + " Highlight");
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getNoOfRowsInTable(Context context) throws SQLException {
        return FolioReaderDB.getInstance(context).getHighlightDao().queryBuilder().countOf();
    }

    public static List<Highlight> getAllRecords(Context context) {
        List<Highlight> records = null;
        try {
            records = FolioReaderDB.getInstance(context).getHighlightDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    public static int deleteAllHighlight(Context context) {
        int count = -1;
        try {
            Dao<Highlight, Integer> dao = FolioReaderDB.getInstance(context).getHighlightDao();
            TableUtils.clearTable(dao.getConnectionSource(), Highlight.class);
            TableUtils.dropTable(dao.getConnectionSource(), Highlight.class, true);
            TableUtils.createTableIfNotExists(dao.getConnectionSource(), Highlight.class);
        } catch (SQLException e) {
            count = -1;
        }
        return count;
    }

    public static boolean isHighlightExistInDB(Context context, Highlight highlight) {
        try {
            if (highlight != null) {
                Dao<Highlight, Integer> dao = FolioReaderDB.getInstance(context).getHighlightDao();
                long count = dao.queryBuilder().setCountOf(true).where()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT_PRE, highlight.getContentPre()).and()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT, highlight.getContent()).and()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT_POST, highlight.getContentPost()).countOf();
                return count > 0;
            } else
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private static Highlight getHighlightIfExistInDB(Context context, Highlight highlight) {
        Highlight record = null;
        try {
            if (highlight != null) {
                Dao<Highlight, Integer> dao = FolioReaderDB.getInstance(context).getHighlightDao();
                record = dao.queryBuilder().where()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT_PRE, highlight.getContentPre()).and()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT, highlight.getContent()).and()
                        .eq(Highlight.LOCAL_DB_HIGHLIGHT_CONTENT_POST, highlight.getContentPost()).queryForFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return record;
        }
        return record;
    }

    public static void save(Context context, Highlight highlight) {
        if (isHighlightExistInDB(context, highlight)) {
            highlight.setId(getHighlightIfExistInDB(context, highlight).getId());
            updateHighlight(context, highlight);
        } else
            createEntryInTable(context, highlight);
    }
}
