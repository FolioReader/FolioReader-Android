package com.folioreader.ui.folio.presenter;

import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;

import org.readium.r2_streamer.model.publication.EpubPublication;

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

    @Override
    public void onReceivePublication(EpubPublication publication) {
        mainMvpView.onLoadPublication(publication);
    }

    @Override
    public void onError() {
        mainMvpView.onError();
    }
}
