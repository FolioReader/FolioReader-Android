package com.folioreader.util;

/**
 * Created by Hrishikesh Kadam on 17/04/2018.
 */
public interface LastReadStateCallback {

    void saveLastReadState(int lastReadChapterIndex, String lastReadSpanIndex);
}
