package com.folioreader.database;

import android.content.Context;
import android.util.Log;

import com.folioreader.model.Highlight;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
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
            Log.d(Tag + "CREAT", status + " Highlight");
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
            Log.d(Tag + "UPDATE", count + " Highlight");
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int updateHighlightStyle(Context context, String id, String type) {
        try {
            UpdateBuilder<Highlight, Integer> updateBuilder = FolioReaderDB.getInstance(context).getHighlightDao().updateBuilder();
            updateBuilder.where().eq(Highlight.LOCAL_DB_HIGHLIGHT_ID, id);
            updateBuilder.updateColumnValue(Highlight.LOCAL_DB_HIGHLIGHT_TYPE, type);
            int count = updateBuilder.update();
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
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
        if (highlight != null) {
            if (isHighlightExistInDB(context, highlight)) {
                highlight.setId(getHighlightIfExistInDB(context, highlight).getId());
                updateHighlight(context, highlight);
            } else {
                createEntryInTable(context, highlight);
            }
        } else
            Log.d(Tag + "SAVE:", "can't save null object");
    }

    public static List<Highlight> getAllHighlight(Context context, String bookId, int pageNo) {
        List<Highlight> highlights = null;
        try {
            Dao<Highlight, Integer> dao = FolioReaderDB.getInstance(context).getHighlightDao();
            highlights = dao.queryBuilder().where()
                    .eq(Highlight.LOCAL_DB_HIGHLIGHT_BOOK_ID, bookId).and()
                    .eq(Highlight.LOCAL_DB_HIGHLIGHT_PAGE, pageNo).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return highlights;
    }

    public static void remove(String highlightId, Context context) {
        int status = -1;
        try {
            Dao<Highlight, Integer> dao = FolioReaderDB.getInstance(context).getHighlightDao();
            DeleteBuilder<Highlight, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Highlight.LOCAL_DB_HIGHLIGHT_ID, highlightId);
            status = deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (status > 0)
            Log.d(Tag + "Remove:", "no of records removed" + status);
        else
            Log.d(Tag + "Remove:", "can't remove");
    }
}

