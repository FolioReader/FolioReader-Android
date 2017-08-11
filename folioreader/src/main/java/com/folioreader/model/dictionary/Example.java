package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Example {
    @JsonProperty
    private String text;

    @Override
    public String toString() {
        return "Example{" +
                "text='" + text + '\'' +
                '}';
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
