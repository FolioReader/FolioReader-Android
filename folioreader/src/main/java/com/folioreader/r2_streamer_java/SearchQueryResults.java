package com.folioreader.r2_streamer_java;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shrikant Badwaik on 10-Mar-17.
 */

//TODO -> to be removed after r2-kotlin-streamer integration
public class SearchQueryResults {
    private int searchCount;
    public List<SearchResult> searchResultList;

    public SearchQueryResults() {
        this.searchResultList = new ArrayList<>();
    }

    public SearchQueryResults(int searchCount, List<SearchResult> searchResultList) {
        this.searchCount = searchCount;
        this.searchResultList = searchResultList;
    }

    public int getSearchCount() {
        searchCount = searchResultList.size();
        return searchCount;
    }

    public List<SearchResult> getSearchResultList() {
        return searchResultList;
    }

    public void setSearchResultList(List<SearchResult> searchResultList) {
        this.searchResultList = searchResultList;
    }
}
