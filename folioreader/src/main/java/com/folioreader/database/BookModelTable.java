package com.folioreader.database;

import android.content.Context;
import android.util.Log;

import com.folioreader.model.BookModel;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;


/**
 * Created by mobisys on 8/10/2016.
 */
public class BookModelTable {
    private static final String Tag = "SmilTable";

    public static int createEntryInTableIfNotExist(Context context, BookModel bookModel) {
        int status = -1;
        try {
            FolioReaderDB.getInstance(context).getBookModelDao().createIfNotExists(bookModel);
        } catch (SQLException e) {
            Log.d(Tag + "CREAT", e.getMessage());
        }

        return status;
    }

    public static BookModel getBookFromName(Context context, String bookName) {
        List<BookModel> bookModels = null;
        try {
            //BookModels = FolioReaderDB.getInstance(context).getBookModelDao().queryForAll();
            QueryBuilder queryBuilder =
                        FolioReaderDB.getInstance(context).getBookModelDao().queryBuilder();
            queryBuilder.where().eq("bookName", bookName);
            bookModels = queryBuilder.query();
        } catch (SQLException e) {
            Log.d(Tag, e.toString());
        }
        if (bookModels != null && bookModels.size() > 0) {
            return bookModels.get(0);
        } else {
            return null;
        }
    }
}
