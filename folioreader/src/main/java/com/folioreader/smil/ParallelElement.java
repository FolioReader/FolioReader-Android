package com.folioreader.smil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the <par> tag.
 * Limitations:
 *   Support only one sequence of audio elements, 
 *   i.e. concurrent play of multiple data source is not supported
 */
public class ParallelElement implements ContainerElement {
    private SequenceElement audioSequence;
    private TextElement textElement;
    private ContainerElement parent;
    
    public ParallelElement(ContainerElement parent) {
        this.parent = parent;
    }

    // Mac complains about this @Override
    public ContainerElement getParent() {
    	return parent;
    }
    
    public TextElement getTextElement() {
        if (textElement != null) {
            return textElement;
        } else {
            return null;
        }
    }
    
    public void setTextElement(TextElement textElement) {
        this.textElement = textElement;
    }

    public void addAudioElement(AudioElement audioElement) {
        audioSequence.add(audioElement);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((audioSequence == null) ? 0 : audioSequence.hashCode());
        result = prime * result
                + ((textElement == null) ? 0 : textElement.hashCode());
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
        if (audioSequence == null) {
            if (other.audioSequence != null) {
                return false;
            }
        } else if (!audioSequence.equals(other.audioSequence)) {
            return false;
        }
        if (textElement == null) {
            if (other.textElement != null) {
                return false;
            }
        } else if (!textElement.equals(other.textElement)) {
            return false;
        }
        return true;
    }
    
    public void setAudioSequence(SequenceElement sequence) {
        audioSequence = sequence;
    }

    public SequenceElement getAudioSequence() {
        return audioSequence;
    }

    public List<AudioElement> getAllAudioElementDepthFirst() {
        if (audioSequence != null) {
            return audioSequence.getAllAudioElementDepthFirst();
        } else {
            return new ArrayList<AudioElement>();
        }
    }

    public List<TextElement> getAllTextElementDepthFirst() {
        return Arrays.asList(textElement);
    }
}
