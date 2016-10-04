package com.folioreader.smil;

import java.util.List;

public interface ContainerElement extends SmilElement {

    /**
     * Useful when navigating the nested data structures.
     *
     * @return the parent SMIL element
     */
    ContainerElement getParent();

    List<AudioElement> getAllAudioElementDepthFirst();

    List<TextElement> getAllTextElementDepthFirst();
}
