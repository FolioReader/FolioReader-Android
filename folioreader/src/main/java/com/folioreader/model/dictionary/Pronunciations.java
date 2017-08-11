package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pronunciations {

    @JsonProperty
    private List<Audio> audio;

    @Override
    public String toString() {
        return "Pronunciations{" +
                "audio=" + audio +
                '}';
    }

    public List<Audio> getAudio() {
        return audio;
    }

    public void setAudio(List<Audio> audio) {
        this.audio = audio;
    }
}
