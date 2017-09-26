package com.folioreader.util;

import com.folioreader.model.Highlight;

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */

public interface OnHighlightCreateListener {

    /**
     * Interface for receiving notification when a highlight is created.
     *
     * @param highlight meta-data for created highlight {@link Highlight}.
     * @param type      type of event e.g new,edit or delete {@link com.folioreader.model.Highlight.HighLightAction}.
     */
    void onCreateHighlight(Highlight highlight, Highlight.HighLightAction type);
}
