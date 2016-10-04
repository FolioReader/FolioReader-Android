package com.folioreader.io;

import junit.framework.TestCase;

public class ExtractXmlEncodingTest extends TestCase {
    static final String UTF_8_ENCODING = "UTF-8";
    final String encodingThatShouldBeMapped = "windows-1252";
    final String validExample = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>";
    final String elidedLine =
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?><!DOCTYPE smil PUBLIC" +
                    "\"-//W3C//DTD SMIL 1.0//EN\" \"http://www.w3.org/TR/REC-SMIL/SMIL10.dtd\" >";


    /**
     * Some books have no newline after the xml element. My code failed to
     * extract the encoding corectly. This test will help me fix my parsing
     * code and make sure I don't make the mistake in future.
     */
    public void testExtractEncodingFromElidedLine() {
        String encoding = ExtractXMLEncoding.extractEncoding(elidedLine);
        assertEquals("iso-8859-1", encoding);
    }

    public void testValidExampleWorks() {
        String encoding = ExtractXMLEncoding.extractEncoding(validExample);
        assertEquals("iso-8859-1", encoding);
    }

    public void testMapUnsupportedCodingWithValidMapping() {
        String mappedEncoding =
                ExtractXMLEncoding.mapUnsupportedEncoding(encodingThatShouldBeMapped);
        assertEquals("iso-8859-1", mappedEncoding);
    }

    public void testWithValueThatShouldNotBeMapped() {
        String unchangedEncoding =
                ExtractXMLEncoding.mapUnsupportedEncoding(UTF_8_ENCODING);
        assertEquals(UTF_8_ENCODING, unchangedEncoding);
    }

}
