package com.folioreader.ui.folio.presenter;

import com.folioreader.ui.base.BaseMvpView;

import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.searcher.SearchQueryResults;

/**
 * @author gautam chibde on 8/6/17.
 */

public interface MainMvpView extends BaseMvpView {
    void onLoadPublication(EpubPublication publication);
    void onShowSearchResults(SearchQueryResults results);
    String getSearchQuery();
}
