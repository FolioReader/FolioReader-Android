package com.folioreader.smil;

import java.io.Serializable;

/**
 * Encapsulates the <text> tag.
 */
public class TextElement implements Serializable,MediaElement {
    private String src;
    private String id;

    public  TextElement(){}
    
    public TextElement(SmilElement parent, String src, String id) {
        this.src = src;
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextElement other = (TextElement) obj;
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
    
    public String getSrc() {
        return src;
    }
}
