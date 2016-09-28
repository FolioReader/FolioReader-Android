package com.folioreader.io;

/**
 * Formats text content as XML
 *
 * @author jharty
 */
public class XmlFormatter {

    /**
     * Formats a string as XML.
     *
     * @param contents
     * @return the formatted string.
     */
    public String formatAsXml(String contents, String tag) {
        return "<" + tag + ">" + contents + "</" + tag + ">";
    }

}
