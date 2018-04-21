package com.folioreader.model.event;

public class SearchEvent {

    private String word;
    private boolean isNewChapter;

    public SearchEvent(){

    }

    public SearchEvent(String word,boolean isNewChapter){
        this.word=word;
        this.isNewChapter = isNewChapter;
    }

    public String getWord() {
        return word;
    }



    public boolean isNewChapter() {
        return isNewChapter;
    }


}
