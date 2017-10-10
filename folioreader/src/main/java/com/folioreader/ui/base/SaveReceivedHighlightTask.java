package com.folioreader.ui.base;

import android.os.AsyncTask;

import com.folioreader.model.HighlightImpl;
import com.folioreader.model.sqlite.HighLightTable;

import java.util.List;

/**
 * Created by gautam on 10/10/17.
 */

public class SaveReceivedHighlightTask extends AsyncTask<Void, Void, Void> {

    private OnSaveHighlight onSaveHighlight;
    private List<HighlightImpl> highlights;
    private String bookId;

    public SaveReceivedHighlightTask(OnSaveHighlight onSaveHighlight,
                                     List<HighlightImpl> highlights,
                                     String bookId) {
        this.onSaveHighlight = onSaveHighlight;
        this.highlights = highlights;
        this.bookId = bookId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (HighlightImpl highlight : highlights) {
            if(highlight.getBookId().equals(bookId)){
                HighLightTable.saveHighlightIfNotExists(highlight);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onSaveHighlight.onFinished();
    }
}
