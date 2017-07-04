package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Audio {
    @JsonProperty
    private String lang;
    @JsonProperty
    private String type;
    @JsonProperty
    private String url;

    @Override
    public String toString() {
        return "Audio{" +
                "lang='" + lang + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
