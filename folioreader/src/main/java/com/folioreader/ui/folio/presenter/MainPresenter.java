package com.folioreader.ui.folio.presenter;

import android.os.AsyncTask;

import com.folioreader.model.HighlightImpl;
import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.ui.base.SaveReceivedHighlightTask;

import org.readium.r2_streamer.model.publication.EpubPublication;

import java.util.List;

/**
 * @author gautam chibde on 8/6/17.
 */

public class MainPresenter implements ManifestCallBack {
    private MainMvpView mainMvpView;

    public MainPresenter(MainMvpView mainMvpView) {
        this.mainMvpView = mainMvpView;
    }

    public void parseManifest(String url) {
        new ManifestTask(this).execute(url);
    }

    public void saveReceivedHighLights(List<HighlightImpl> highlights, OnSaveHighlight onSaveHighlight, String bookId) {
        new SaveReceivedHighlightTask(onSaveHighlight, highlights, bookId).execute();
    }

    @Override
    public void onReceivePublication(EpubPublication publication) {
        mainMvpView.onLoadPublication(publication);
    }

    @Override
    public void onError() {
        mainMvpView.onError();
    }
}
