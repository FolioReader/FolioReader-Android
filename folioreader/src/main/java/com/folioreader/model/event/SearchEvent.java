package com.folioreader.model.event;

public class SearchEvent {

    private String word;
    private String id;
    private boolean isNewChapter;
    private int count;

    public SearchEvent() {

    }

    public SearchEvent(String word, boolean isNewChapter, int count,String uniqueId) {
        this.id = uniqueId;
        this.word = word;
        this.count = count;
        this.isNewChapter = isNewChapter;
    }

    public String getWord() {
        return word;
    }


    public boolean isNewChapter() {
        return isNewChapter;
    }

    public String getId() {
        return id;
    }

    public int getCount() {
        return count;
    }
}
