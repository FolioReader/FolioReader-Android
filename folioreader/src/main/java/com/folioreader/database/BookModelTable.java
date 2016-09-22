package com.folioreader.database;

import android.content.Context;
import android.util.Log;

import com.folioreader.model.BookModel;
import com.folioreader.model.Highlight;
import com.folioreader.model.SmilElements;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;



/**
 * Created by mobisys on 8/10/2016.
 */
public class BookModelTable {
    private static final String Tag = "SmilTable";
    public static int createEntryInTableIfNotExist(Context context, BookModel BookModel) {
        int status = -1;
        try {
                FolioReaderDB.getInstance(context).getBookModelDao().createIfNotExists(BookModel);
        } catch (SQLException e) {
            Log.d(Tag + "CREAT", e.getMessage());
        }

        return status;
    }

    public static BookModel getAllRecords(Context context) {
        List<BookModel> BookModels=null;
        try {
            BookModels = FolioReaderDB.getInstance(context).getBookModelDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         if(BookModels!=null) {
             return BookModels.get(0);
         } else return null;
    }
}
