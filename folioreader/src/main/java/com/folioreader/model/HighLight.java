package com.folioreader.model;

import java.util.Date;

/**
 * Interface to access Highlight data.
 *
 * @author gautam on 9/10/17.
 */

public interface HighLight {

    /**
     * Highlight action
     */
    enum HighLightAction {
        NEW, DELETE, MODIFY
    }

    int getId();

    /**
     * Returns Book id.
     */
    String getBookId();

    /**
     * Returns Highlighted text content text content.
     */
    String getContent();

    /**
     * Returns Date time when highlight is created (format:- MMM dd, yyyy | HH:mm).
     */
    Date getDate();

    /**
     * Returns Field defines the color of the highlight.
     */
    String getType();

    /**
     * Returns Page index in the book taken from Epub spine reference.
     */
    int getPageNumber();

    /**
     * Returns href of the page from the Epub spine list.
     */
    String getPageId();

    /**
     * <p> Contains highlight meta data in terms of rangy format.</p>
     * <strong>format </strong>:- start$end$id$class$containerId.
     * <p>for reference, look here: <a href="https://github.com/timdown/rangy">rangy</a>.</p>
     */
    String getRangy();

    /**
     * Returns Note linked to the highlight (optional)
     */
    String getNote();
}
