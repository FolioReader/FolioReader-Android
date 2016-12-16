package com.folioreader.smil;

import android.os.Parcel;
import android.os.Parcelable;

/*import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;*/

/**
 * Encapsulates the <audio> tag.
 */
public class AudioElement implements Parcelable, MediaElement {
    String src;
    int clipBegin;
    int clipEnd;
    SmilElement parent;


    public AudioElement() {
    }

    public AudioElement(SmilElement parent, String src, int clipBegin, int clipEnd) {
        super();
        this.parent = parent;
        this.src = src;
        this.clipBegin = clipBegin;
        this.clipEnd = clipEnd;
    }

    public AudioElement(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static final Creator<AudioElement> CREATOR = new Creator<AudioElement>() {
        @Override
        public AudioElement createFromParcel(Parcel in) {
            return new AudioElement(in);
        }

        @Override
        public AudioElement[] newArray(int size) {
            return new AudioElement[size];
        }
    };

    public String getSrc() {
        return src;
    }

    public double getClipBegin() {
        return clipBegin;
    }

    public double getClipEnd() {
        return clipEnd;
    }

    /**
     * Gets the text element in the same <par> section
     *
     * @return
     */
    public TextElement getCompanionTextElement() {
        if (parent instanceof ParallelElement) {
            return ((ParallelElement) parent).getTextElement();
        } else {
            return null;
        }
    }

    /**
     * Whether the given time is in the clip of this audio element
     *
     * @param time
     * @return
     */
    public boolean inClip(double time) {
        return time < clipEnd && time >= clipBegin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(clipBegin);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(clipEnd);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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

        if (!(obj instanceof AudioElement)) {
            return false;
        }

        AudioElement other = (AudioElement) obj;
        if (Double.doubleToLongBits(clipBegin) != Double
                .doubleToLongBits(other.clipBegin)) {
            return false;
        }
        if (Double.doubleToLongBits(clipEnd) != Double
                .doubleToLongBits(other.clipEnd)) {
            return false;
        }
        if (src == null) {
            if (other.src != null) {
                return false;
            }
        } else if (!src.equals(other.src)) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) parent, flags);
        dest.writeString(src);
        dest.writeInt(clipBegin);
        dest.writeInt(clipEnd);
    }

    private void readFromParcel(Parcel in) {
        parent = in.readParcelable(SmilElement.class.getClassLoader());
        src = in.readString();
        clipBegin = in.readInt();
        clipEnd = in.readInt();
    }
}
