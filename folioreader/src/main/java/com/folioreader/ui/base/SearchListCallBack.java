package com.folioreader.ui.base;

import org.readium.r2_streamer.model.searcher.SearchQueryResults;

public interface SearchListCallBack extends BaseMvpView {
    void onReceiveSearchList(SearchQueryResults searchQueryResults);
}