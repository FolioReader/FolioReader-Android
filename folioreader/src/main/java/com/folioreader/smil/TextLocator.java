package com.folioreader.smil;

import com.folioreader.DummyDtdResolver;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

/**
 * Helper Class for retrieving the text for TextElement
 * Currently it only works with local files.
 */
public class TextLocator extends DefaultHandler {

    private File mBaseDirectory;
    private String mTargetId;
    private String mResult;
    private int mDepth = 0;
    private static final Logger log = Logger.getAnonymousLogger();
    private CharArrayWriter mValue = new CharArrayWriter();

    public TextLocator(File mBaseDirectory) {
        this.mBaseDirectory = mBaseDirectory;
    }

    public String getText(String src) throws IOException {
        if (src.contains("#")) {
            mTargetId = src.substring(src.indexOf('#') + 1);
            File file = new File(mBaseDirectory, src.substring(0, src.indexOf('#')));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                InputSource input = new InputSource(new FileInputStream(file));
                XMLReader saxParser = factory.newSAXParser().getXMLReader();
                saxParser.setEntityResolver(new DummyDtdResolver());
                saxParser.setContentHandler(this);
                saxParser.parse(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            File file = new File(mBaseDirectory, src);
            long len = file.length();
            FileInputStream fis = new FileInputStream(file);
            byte []buf = new byte[(int) len];
            fis.read(buf);
            fis.close();
            mResult = new String(buf, Charset.forName("UTF-8"));
        }
        return mResult;
    }

    @Override
    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {

        mValue.reset();
        String id = attributes.getValue("id");
        log.info(name + ": " + id);
        if (id != null && id.equals(mTargetId)) {
            mDepth++;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (mDepth > 0) {
            mValue.write(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) {
        if (mDepth > 0) {
            mDepth--;
            mResult = mValue.toString();
        }
    }

}
