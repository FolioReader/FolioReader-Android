package com.folioreader.database;

import android.content.Context;
import android.util.Log;

import com.folioreader.model.BookModel;

import java.sql.SQLException;


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
        BookModel bookModel = null;
        try {
            bookModel = FolioReaderDB.getInstance(context).getBookModelDao()
                    .queryBuilder().where().eq("bookName", bookName)
                    .queryForFirst();
        } catch (SQLException e) {
            Log.d(Tag, e.toString());
        }
        return bookModel;
    }
}
