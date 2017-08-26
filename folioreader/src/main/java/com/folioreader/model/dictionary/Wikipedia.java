package com.folioreader.model.dictionary;

/**
 * Created by gautam on 7/7/17.
 */

public class Wikipedia {
    private String word;
    private String definition;
    private String link;

    public Wikipedia() {
    }

    @Override
    public String toString() {
        return "Wikipedia{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
