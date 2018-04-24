package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.folioreader.util.ObjectMapperSingleton;

/**
 * Created by Hrishikesh Kadam on 20/04/2018.
 */
public class ReadPositionImpl implements ReadPosition, Parcelable {

    public static final Creator<ReadPositionImpl> CREATOR = new Creator<ReadPositionImpl>() {
        @Override
        public ReadPositionImpl createFromParcel(Parcel in) {
            return new ReadPositionImpl(in);
        }

        @Override
        public ReadPositionImpl[] newArray(int size) {
            return new ReadPositionImpl[size];
        }
    };

    private static final String LOG_TAG = ReadPositionImpl.class.getSimpleName();
    private String bookId;
    private int chapterIndex = -1;
    private String chapterHref;
    private boolean usingId;
    private String value;

    public ReadPositionImpl() {
    }

    public ReadPositionImpl(String bookId, int chapterIndex, String chapterHref, boolean usingId, String value) {
        this.bookId = bookId;
        this.chapterIndex = chapterIndex;
        this.chapterHref = chapterHref;
        this.usingId = usingId;
        this.value = value;
    }

    protected ReadPositionImpl(Parcel in) {
        bookId = in.readString();
        chapterIndex = in.readInt();
        chapterHref = in.readString();
        usingId = in.readByte() != 0;
        value = in.readString();
    }

    @Override
    public String toString() {
        return "ReadPositionImpl{" +
                "bookId='" + bookId + '\'' +
                ", chapterIndex=" + chapterIndex +
                ", chapterHref='" + chapterHref + '\'' +
                ", usingId=" + usingId +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    @Override
    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    @Override
    public String getChapterHref() {
        return chapterHref;
    }

    public void setChapterHref(String chapterHref) {
        this.chapterHref = chapterHref;
    }

    @Override
    public boolean isUsingId() {
        return usingId;
    }

    public void setUsingId(boolean usingId) {
        this.usingId = usingId;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toJson() {
        try {
            ObjectWriter objectWriter = ObjectMapperSingleton.getObjectMapper().writer();
            return objectWriter.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(LOG_TAG, "-> " + e);
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(bookId);
        dest.writeInt(chapterIndex);
        dest.writeString(chapterHref);
        dest.writeByte((byte) (usingId ? 1 : 0));
        dest.writeString(value);
    }
}
