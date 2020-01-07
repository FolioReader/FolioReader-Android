package com.folioreader.model.dictionary;

import java.util.List;

public class VietnameseDictionary {
    private List<VietnameseDictionaryResult> resultsList;

    public VietnameseDictionary(List<VietnameseDictionaryResult> resultslist) {
        setResult(resultslist);
    }

    public List<VietnameseDictionaryResult> getResultsList() {
        return resultsList;
    }

    public void setResult(List<VietnameseDictionaryResult> resultlist) {
        this.resultsList = resultlist;
    }
}
