package com.folioreader.model;

import com.folioreader.FolioReader;
import com.folioreader.ui.folio.activity.FolioActivity;

import org.readium.r2_streamer.model.publication.EpubPublication;

/**
 * Interface contract for last read position
 *
 * @author Hrishikesh Kadam on 20/04/2018.
 */
public interface ReadPosition {

    /**
     * Returns the bookId if sent in {@link FolioReader}'s openBook() else logic defined in
     * {@link FolioActivity#onLoadPublication(EpubPublication)} will return the bookId.
     */
    String getBookId();

    /**
     * Returns the idref of the spine item.
     */
    String getChapterId();

    /**
     * Returns the href of the manifest item.
     */
    String getChapterHref();

    /**
     * Returns the chapter index from spine tag
     */
    int getChapterIndex();

    /**
     * Returns true if span tag has id
     */
    boolean isUsingId();

    /**
     * Returns the span id, if usingId true or else returns span index
     */
    String getValue();

    /**
     * Returns the json format of this object
     */
    String toJson();
}
