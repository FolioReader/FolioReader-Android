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
public class Dictionary {
    @JsonProperty
    private int status;
    @JsonProperty
    private String url;
    @JsonProperty
    private List<DictionaryResults> results;

    @Override
    public String toString() {
        return "Dictionary{" +
                "status=" + status +
                ", url='" + url + '\'' +
                ", results=" + results +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<DictionaryResults> getResults() {
        return results;
    }

    public void setResults(List<DictionaryResults> results) {
        this.results = results;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
