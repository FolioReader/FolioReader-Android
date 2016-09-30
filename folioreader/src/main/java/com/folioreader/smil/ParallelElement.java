package com.folioreader.smil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the <par> tag.
 * Limitations:
 * Support only one sequence of audio elements,
 * i.e. concurrent play of multiple data source is not supported
 */
public class ParallelElement implements ContainerElement {
    private SequenceElement mAudioSequence;
    private TextElement mTextElement;
    private ContainerElement mParent;

    public ParallelElement(ContainerElement parent) {
        this.mParent = parent;
    }

    // Mac complains about this @Override
    public ContainerElement getParent() {
        return mParent;
    }

    public TextElement getTextElement() {
        if (mTextElement != null) {
            return mTextElement;
        } else {
            return null;
        }
    }

    public void setTextElement(TextElement textElement) {
        this.mTextElement = textElement;
    }

    public void addAudioElement(AudioElement audioElement) {
        mAudioSequence.add(audioElement);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mAudioSequence == null) ? 0 : mAudioSequence.hashCode());
        result = prime * result
                + ((mTextElement == null) ? 0 : mTextElement.hashCode());
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
        ParallelElement other = (ParallelElement) obj;
        if (mAudioSequence == null) {
            if (other.mAudioSequence != null) {
                return false;
            }
        } else if (!mAudioSequence.equals(other.mAudioSequence)) {
            return false;
        }
        if (mTextElement == null) {
            if (other.mTextElement != null) {
                return false;
            }
        } else if (!mTextElement.equals(other.mTextElement)) {
            return false;
        }
        return true;
    }

    public void setAudioSequence(SequenceElement sequence) {
        mAudioSequence = sequence;
    }

    public SequenceElement getAudioSequence() {
        return mAudioSequence;
    }

    public List<AudioElement> getAllAudioElementDepthFirst() {
        if (mAudioSequence != null) {
            return mAudioSequence.getAllAudioElementDepthFirst();
        } else {
            return new ArrayList<AudioElement>();
        }
    }

    public List<TextElement> getAllTextElementDepthFirst() {
        return Arrays.asList(mTextElement);
    }
}
