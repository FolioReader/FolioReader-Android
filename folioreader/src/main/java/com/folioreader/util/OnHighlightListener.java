package com.folioreader.util;

import com.folioreader.model.Highlight;

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */

public interface OnHighlightListener {

    /**
     * This method will be invoked when a highlight is created, deleted or modified.
     *
     * @param highlight meta-data for created highlight {@link Highlight}.
     * @param type      type of event e.g new,edit or delete {@link com.folioreader.model.Highlight.HighLightAction}.
     */
    void onHighlight(Highlight highlight, Highlight.HighLightAction type);
}
