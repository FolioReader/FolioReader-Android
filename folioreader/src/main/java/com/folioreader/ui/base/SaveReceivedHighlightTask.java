package com.folioreader.ui.base;

import android.os.AsyncTask;
import com.folioreader.model.HighLight;
import com.folioreader.model.sqlite.HighLightTable;

import java.util.List;

/**
 * Background task to save received highlights.
 * <p>
 * Created by gautam on 10/10/17.
 */
public class SaveReceivedHighlightTask extends AsyncTask<Void, Void, Void> {

    private OnSaveHighlight onSaveHighlight;
    private List<HighLight> highLights;

    public SaveReceivedHighlightTask(OnSaveHighlight onSaveHighlight,
                                     List<HighLight> highLights) {
        this.onSaveHighlight = onSaveHighlight;
        this.highLights = highLights;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (HighLight highLight : highLights) {
            HighLightTable.saveHighlightIfNotExists(highLight);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onSaveHighlight.onFinished();
    }
}
