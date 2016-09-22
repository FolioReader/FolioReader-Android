package com.folioreader.smil;

import java.io.Serializable;

/**
 * Encapsulates the <audio> tag.
 */
public class AudioElement implements Serializable,MediaElement {
    
    private String src;
    private int clipBegin;
    private int clipEnd;
    private String id;
    private SmilElement parent;


    public  AudioElement(){}

    public AudioElement(SmilElement parent, String src, int clipBegin, int clipEnd, String id) {
        super();
        this.parent = parent;
        this.src = src;
        this.clipBegin = clipBegin;
        this.clipEnd = clipEnd;
        this.id = id;
    }
    
    public String getSrc() {
        // TODO Auto-generated method stub
        return src;
    }

    public double getClipBegin() {
        // TODO Auto-generated method stub
        return clipBegin;
    }

    public double getClipEnd() {
        // TODO Auto-generated method stub
        return clipEnd;
    }

    public String getId() {
        // TODO Auto-generated method stub
        return id;
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
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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
    
}
