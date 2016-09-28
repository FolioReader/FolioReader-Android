package com.folioreader.smil;

import java.io.Serializable;

/**
 * Encapsulates the <audio> tag.
 */
public class AudioElement implements Serializable, MediaElement {

    private String mSrc;
    private int mClipBegin;
    private int mClipEnd;
    private String mId;
    private SmilElement mParent;


    public AudioElement() {
    }

    public AudioElement(SmilElement mParent, String mSrc, int mClipBegin, int clipEnd, String mId) {
        super();
        this.mParent = mParent;
        this.mSrc = mSrc;
        this.mClipBegin = mClipBegin;
        this.mClipEnd = clipEnd;
        this.mId = mId;
    }

    public String getSrc() {
        // TODO Auto-generated method stub
        return mSrc;
    }

    public double getClipBegin() {
        // TODO Auto-generated method stub
        return mClipBegin;
    }

    public double getmClipEnd() {
        // TODO Auto-generated method stub
        return mClipEnd;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return mId;
    }

    /**
     * Gets the text element in the same <par> section
     *
     * @return
     */
    public TextElement getCompanionTextElement() {
        if (mParent instanceof ParallelElement) {
            return ((ParallelElement) mParent).getTextElement();
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
        return time < mClipEnd && time >= mClipBegin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(mClipBegin);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mClipEnd);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((mId == null) ? 0 : mId.hashCode());
        result = prime * result + ((mSrc == null) ? 0 : mSrc.hashCode());
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
        if (Double.doubleToLongBits(mClipBegin) != Double
                .doubleToLongBits(other.mClipBegin)) {
            return false;
        }
        if (Double.doubleToLongBits(mClipEnd) != Double
                .doubleToLongBits(other.mClipEnd)) {
            return false;
        }
        if (mId == null) {
            if (other.mId != null) {
                return false;
            }
        } else if (!mId.equals(other.mId)) {
            return false;
        }
        if (mSrc == null) {
            if (other.mSrc != null) {
                return false;
            }
        } else if (!mSrc.equals(other.mSrc)) {
            return false;
        }
        return true;
    }

}
