package com.folioreader.smil;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the <seq> tag.
 */
public class SequenceElement implements ContainerElement {

    private double mDuration;
    private List<SmilElement> mElements = new ArrayList<SmilElement>();
    private ContainerElement mParent;

    public SequenceElement(ContainerElement parent, double duration) {
        this.mParent = parent;
        this.mDuration = duration;
    }

    public SequenceElement(ContainerElement parent) {
        this.mParent = parent;
    }

    public void add(SmilElement currentElement) {
        mElements.add(currentElement);
    }

    // Mac complains about @Override
    public ContainerElement getParent() {
        return mParent;
    }

    public SmilElement get(int i) {
        return mElements.get(i);
    }

    public int size() {
        return mElements.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(mDuration);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((mElements == null) ? 0 : mElements.hashCode());
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
        SequenceElement other = (SequenceElement) obj;
        if (Double.doubleToLongBits(mDuration) != Double
                .doubleToLongBits(other.mDuration)) {
            return false;
        }
        if (mElements == null) {
            if (other.mElements != null) {
                return false;
            }
        } else if (!mElements.equals(other.mElements)) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return mElements.isEmpty();
    }

    public List<AudioElement> getAllAudioElementDepthFirst() {
        List<AudioElement> ret = new ArrayList<AudioElement>();
        for (SmilElement elem : mElements) {
            if (elem instanceof ContainerElement) {
                ret.addAll(((ContainerElement) elem).getAllAudioElementDepthFirst());
            } else if (elem instanceof AudioElement) {
                ret.add((AudioElement) elem);
            }
        }
        return ret;
    }

    public List<TextElement> getAllTextElementDepthFirst() {
        List<TextElement> ret = new ArrayList<TextElement>();
        for (SmilElement elem : mElements) {
            if (elem instanceof ContainerElement) {
                ret.addAll(((ContainerElement) elem).getAllTextElementDepthFirst());
            } else if (elem instanceof TextElement) {
                ret.add((TextElement) elem);
            }
        }
        return ret;
    }

    /**
     * @return all the mElements found in the SMIL file.
     */
    public List<SmilElement> getElements() {
        return mElements;

    }
}
