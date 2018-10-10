package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.folioreader.util.ObjectMapperSingleton;

import java.io.IOException;

/**
 * Created by Hrishikesh Kadam on 20/04/2018.
 */
@JsonPropertyOrder({"bookId", "chapterHref", "usingId", "value"})
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private String chapterHref;
    private boolean usingId;
    private String value;

    public ReadPositionImpl() {
    }

    public ReadPositionImpl(String bookId, String chapterHref, boolean usingId, String value) {
        this.bookId = bookId;
        this.chapterHref = chapterHref;
        this.usingId = usingId;
        this.value = value;
    }

    public static ReadPosition createInstance(String jsonString) {

        ReadPositionImpl readPosition = null;
        try {
            readPosition = ObjectMapperSingleton.getObjectMapper()
                    .reader()
                    .forType(ReadPositionImpl.class)
                    .readValue(jsonString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "-> ", e);
        }
        return readPosition;
    }

    protected ReadPositionImpl(Parcel in) {
        bookId = in.readString();
        chapterHref = in.readString();
        usingId = in.readByte() != 0;
        value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookId);
        dest.writeString(chapterHref);
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
            Log.e(LOG_TAG, "-> ", e);
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
