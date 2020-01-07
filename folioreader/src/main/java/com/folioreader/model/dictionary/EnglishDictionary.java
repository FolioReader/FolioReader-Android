package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * class is object representation of JSON received from
 * open source dictionary API "pearson"
 * ref => http://developer.pearson.com/apis/dictionaries
 *
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnglishDictionary {
    @JsonProperty
    private int status;
    @JsonProperty
    private String url;
    @JsonProperty
    private List<EnglishDictionaryResults> resultsList;

    @Override
    public String toString() {
        return "EnglishDictionary{" +
                "status=" + status +
                ", url='" + url + '\'' +
                ", results=" + resultsList +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<EnglishDictionaryResults> getResultsList() {
        return resultsList;
    }

    public void setResults(List<EnglishDictionaryResults> resultslist) {
        this.resultsList = resultslist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
