package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.folioreader.util.ObjectMapperSingleton;

/**
 * Created by Hrishikesh Kadam on 20/04/2018.
 */
@JsonPropertyOrder({"bookId", "chapterId", "chapterHref", "chapterIndex", "usingId", "value"})
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
    private String chapterId;
    private String chapterHref;
    private int chapterIndex = -1;
    private boolean usingId;
    private String value;

    public ReadPositionImpl() {
    }

    public ReadPositionImpl(String bookId, String chapterId, String chapterHref, int chapterIndex,
                            boolean usingId, String value) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.chapterHref = chapterHref;
        this.chapterIndex = chapterIndex;
        this.usingId = usingId;
        this.value = value;
    }

    protected ReadPositionImpl(Parcel in) {
        bookId = in.readString();
        chapterId = in.readString();
        chapterHref = in.readString();
        chapterIndex = in.readInt();
        usingId = in.readByte() != 0;
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(chapterId);
        dest.writeString(chapterHref);
        dest.writeInt(chapterIndex);
        dest.writeByte((byte) (usingId ? 1 : 0));
        dest.writeString(value);
    }

    @Override
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    @Override
    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
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
}
