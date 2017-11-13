package com.folioreader.model.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictionaryResults {
    @JsonProperty
    private String headword;
    @JsonProperty
    private String part_of_speech;
    @JsonProperty
    private List<Pronunciations> pronunciations;
    @JsonProperty
    private List<Senses> senses;


    @Override
    public String toString() {
        return "DictionaryResults{" +
                "headword='" + headword + '\'' +
                ", part_of_speech='" + part_of_speech + '\'' +
                ", pronunciations=" + pronunciations +
                ", senses=" + senses +
                '}';
    }

    public String getHeadword() {
        return headword;
    }

    public void setHeadword(String headword) {
        this.headword = headword;
    }

    public String getPartOfSpeech() {
        return part_of_speech;
    }

    public void setPartOfSpeech(String part_of_speech) {
        this.part_of_speech = part_of_speech;
    }

    public List<Pronunciations> getPronunciations() {
        return pronunciations;
    }

    public void setPronunciations(List<Pronunciations> pronunciations) {
        this.pronunciations = pronunciations;
    }

    public List<Senses> getSenses() {
        return senses;
    }

    public void setSenses(List<Senses> senses) {
        this.senses = senses;
    }
}
