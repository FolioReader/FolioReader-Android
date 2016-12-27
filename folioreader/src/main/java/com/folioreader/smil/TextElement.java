package com.folioreader.smil;

import android.os.Parcel;
import android.os.Parcelable;



/**
 * Encapsulates the <text> tag.
 */
public class TextElement implements Parcelable, MediaElement {
    String src;

    public  TextElement(){}
    
    public TextElement(SmilElement parent, String src) {
        this.src = src;
    }

    public TextElement(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static final Creator<TextElement> CREATOR = new Creator<TextElement>() {
        @Override
        public TextElement createFromParcel(Parcel in) {
            return new TextElement(in);
        }

        @Override
        public TextElement[] newArray(int size) {
            return new TextElement[size];
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextElement other = (TextElement) obj;
        if (src == null) {
            if (other.src != null) {
                return false;
            }
        } else if (!src.equals(other.src)) {
            return false;
        }
        return true;
    }
    
    public String getSrc() {
        return src;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
    }

    private void readFromParcel(Parcel in) {
        src = in.readString();
    }
}
