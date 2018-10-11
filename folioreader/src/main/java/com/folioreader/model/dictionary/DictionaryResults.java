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
    private String partOfSpeech;
    @JsonProperty
    private List<Pronunciations> pronunciations;
    @JsonProperty
    private List<Senses> senses;


    @Override
    public String toString() {
        return "DictionaryResults{" +
                "headword='" + headword + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
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
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
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
