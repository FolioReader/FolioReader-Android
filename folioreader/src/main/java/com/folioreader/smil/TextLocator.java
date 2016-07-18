package com.folioreader.smil;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.folioreader.DummyDtdResolver;

/**
 * Helper Class for retrieving the text for TextElement
 * Currently it only works with local files.
 */
public class TextLocator extends DefaultHandler {
    
    private File baseDirectory;
    private String targetId;
    private String result;
    private int depth = 0;
    private Logger log = Logger.getAnonymousLogger();
    private CharArrayWriter value = new CharArrayWriter();
    
    public TextLocator(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    
    public String getText(String src) throws IOException {
        if (src.contains("#")) {
            targetId = src.substring(src.indexOf('#') + 1);
            File file = new File(baseDirectory, src.substring(0, src.indexOf('#')));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
            	org.xml.sax.InputSource input = new InputSource(new FileInputStream(file));
            	XMLReader saxParser = factory.newSAXParser().getXMLReader();
            	saxParser.setEntityResolver(new DummyDtdResolver());
            	saxParser.setContentHandler(this);
            	saxParser.parse(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            File file = new File(baseDirectory, src); 
            long len = file.length();
            FileInputStream fis = new FileInputStream(file);
            byte buf[] = new byte[(int) len];
            fis.read(buf);
            fis.close();
            result = new String(buf);
        }
        return result;
    }
    
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
    	
    	value.reset();
        String id = attributes.getValue("id");
        log.info(name + ": " + id);
        if (id != null && id.equals(targetId)) {
            depth++;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (depth > 0) {
        	value.write(ch, start, length);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String name) {
    	if (depth > 0) {
    		depth--;
    		result = value.toString();
    	}
    }
    
}
