package com.folioreader.ui.folio.presenter;

import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;
import com.folioreader.ui.base.SearchListCallBack;
import com.folioreader.ui.base.SearchListTask;

import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.searcher.SearchQueryResults;

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

    public void searchQuery(String searchUrl){
        new SearchListTask(new SearchListCallBack() {
            @Override
            public void onReceiveSearchList(SearchQueryResults searchQueryResults) {
                mainMvpView.onShowSearchResults(searchQueryResults);
            }

            @Override
            public void onError() {
                // TODO: 20.04.2018
            }
        }).execute(searchUrl);
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
