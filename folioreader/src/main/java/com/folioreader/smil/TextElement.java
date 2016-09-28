package com.folioreader.smil;

import java.io.Serializable;

/**
 * Encapsulates the <text> tag.
 */
public class TextElement implements Serializable, MediaElement {
    private String mSrc;
    private String mId;

    public  TextElement(){}
    
    public TextElement(SmilElement parent, String mSrc, String mId) {
        this.mSrc = mSrc;
        this.mId = mId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextElement other = (TextElement) obj;
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
    
    public String getSrc() {
        return mSrc;
    }
}
