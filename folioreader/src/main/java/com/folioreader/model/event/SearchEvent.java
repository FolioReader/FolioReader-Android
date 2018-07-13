package com.folioreader.model.event;

public class SearchEvent {

    private String mWord;
    private String mId;
    private boolean mNewChapter;
    private int mCount;

    public SearchEvent() {
    }

    public SearchEvent(String mWord, boolean mNewChapter, int mCount, String mUniqueId) {
        this.mId = mUniqueId;
        this.mWord = mWord;
        this.mCount = mCount;
        this.mNewChapter = mNewChapter;
    }

    public String getWord() {
        return mWord;
    }

    public boolean isNewChapter() {
        return mNewChapter;
    }

    public String getId() {
        return mId;
    }

    public int getCount() {
        return mCount;
    }
}
