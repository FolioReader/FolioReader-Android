package com.folioreader.model.media_overlay;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author gautam chibde on 13/6/17.
 */

public class OverlayItems implements Parcelable {
    private String id;
    private String tag;
    private String classType;
    private String spineHref;

    public OverlayItems() {
    }

    public OverlayItems(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    public OverlayItems(String id, String tag,String spineHref) {
        this.id = id;
        this.tag = tag;
        this.spineHref = spineHref;
    }

    protected OverlayItems(Parcel in) {
        id = in.readString();
        tag = in.readString();
        classType = in.readString();
        spineHref = in.readString();
    }

    public static final Creator<OverlayItems> CREATOR = new Creator<OverlayItems>() {
        @Override
        public OverlayItems createFromParcel(Parcel in) {
            return new OverlayItems(in);
        }

        @Override
        public OverlayItems[] newArray(int size) {
            return new OverlayItems[size];
        }
    };

    @Override
    public String toString() {
        return "OverlayItems{" +
                "id='" + id + '\'' +
                ", tag='" + tag + '\'' +
                ", classType='" + classType + '\'' +
                ", spineHref='" + spineHref + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getSpineHref() {
        return spineHref;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(tag);
        dest.writeString(classType);
        dest.writeString(spineHref);
    }
}
