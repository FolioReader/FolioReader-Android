package com.folioreader.model;

/**
 * Created by Hrishikesh Kadam on 20/04/2018.
 */
public interface ReadPosition {

    String getBookId();

    int getChapterIndex();

    String getChapterHref();

    boolean isUsingId();

    String getValue();

    String toJson();
}
