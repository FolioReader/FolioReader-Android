package com.folioreader.io;

import junit.framework.TestCase;

/**
 * Some basic tests for our XMLWriter class.
 *
 * @author jharty
 */
public class XmlWriterTest extends TestCase {

    XmlFormatter xmlFormatter;

    protected void setUp() {
        xmlFormatter = new XmlFormatter();
    }

    public void testVersionNumberFormattedAsXml() {
        assertEquals("<version>1.0.298.1</version>",
                xmlFormatter.formatAsXml("1.0.298.1", "version"));
    }

    public void testAgeFormattedAsXML() {
        assertEquals("<age>47</age>", xmlFormatter.formatAsXml("47", "age"));
    }

}
