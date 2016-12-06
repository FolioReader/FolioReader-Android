package com.folioreader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;


// The EntityResolver prevents the SAX parser from trying to
// download external entities e.g. xhtml1-strict.dtd from
// the referenced URI. Having our own entity resolver makes
// the tests both faster, as they don't need to visit the web;
// and more reliable, as the w3c site returns a HTTP 503 to
// requests for this file from the SAX parser (it loads OK in
// a web browser).
// Thanks to: http://forums.sun.com/thread.jspa?threadID=413937
// for the following code and fix.
public class DummyDtdResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, java.io.IOException {
        if (systemId.endsWith(".dtd")) {
            return new InputSource(new ByteArrayInputStream(
                    "<?xml version='1.0' encoding='UTF-8'?>".getBytes(Charset.forName("UTF-8"))));
        } else {
            return null;
        }
    }
}
