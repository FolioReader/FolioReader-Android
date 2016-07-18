/**
 * Parser for version 1 Smil files.
 * 
 * Extracts the contents of the files, including the sequence of SEQ 
 * (sequential) and PAR (parallel) elements. These often contain additional
 * elements e.g. text and audio contents.
 * 
 * TODO(jharty):
 *   We need to determine:
 *   1. Can PAR elements be nested
 *   2. Can SEQ elements be nested
 *   
 *   I also need to determine whether the current state machine is the
 *   appropriate model to represent the contents of SMIL files. It's certainly
 *   broken currently.
 */
package com.folioreader.smil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.folioreader.DummyDtdResolver;
import com.folioreader.util.AppUtil;

/**
 * Parser for SMIL files.
 * 
 * TODO(jharty): Think carefully how to test the parsing for more complex
 * contents. The state transitions may be error-prone and the code in
 * startElement in particular is of concern.
 * 
 * Note: Malformed or invalid content of a SMIL file stops elements from being
 * created. At the moment no error is reported in such cases. This was
 * evidenced when I tested with a simple file containing a single audio
 * element, when no elements were generated until I had added all the
 * attributes correctly. We could add informational messages to the create
 * methods.
 * 
 * @author jharty
 *
 */
public class SmilParser extends DefaultHandler {
    
    private enum State {
        INIT,
        SEQ,
        PARA,
    }
    private Logger log = Logger.getLogger(SmilParser.class.getSimpleName());
    private ContainerElement currentElement;
    private Attributes attributes;
    private State state;
    private SequenceElement rootSequence;
   
    /**
     * Parse contents that should contain the Smil File structure.
     * 
     * Useful for testing the parsing.
     * 
     * @param content the content to parse
     * @return Sequence of Elements.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public SequenceElement parse(String content) throws IOException, SAXException, ParserConfigurationException {
       return this.parse(new ByteArrayInputStream(content.getBytes()));
    }
    
    /**
     * Parses the input stream of content with a default encoding of UTF-8. 
     * 
     * Calls parse(InputStream stream, String encoding);
     */
    public SequenceElement parse(InputStream stream) throws IOException, SAXException, ParserConfigurationException {
        return parse(stream, "UTF-8");
    }

    // Many thanks to the following link which provided the final piece for me,
    // http://stackoverflow.com/questions/293728/e-is-not-correctly-parsed
    public SequenceElement parse(InputStream stream, String encoding) throws IOException, SAXException, ParserConfigurationException {
        state = State.INIT;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setEntityResolver(new DummyDtdResolver());
        parser.setContentHandler(this);
        org.xml.sax.InputSource input = new InputSource(stream);
        input.setEncoding(encoding);

        parser.parse(input);
        return rootSequence;
    }

    /**
     * Called by the SAX Parser for the start of every element discovered.
     * 
     * TODO(jharty): Rework, test, and simplify.
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        String name = localName.length() != 0 ? localName : qName;
        this.attributes = attributes;
        switch (state) {
            case INIT: {
                log.info("init " + name);
                if ("seq".equalsIgnoreCase(name)) {
                    state = State.SEQ;
                    rootSequence = createSequenceElement();
                    currentElement = rootSequence;
                }
                break;
            }
            case SEQ: {
                log.info("seq " + name);
                SequenceElement seq = (SequenceElement) currentElement;
                if ("par".equalsIgnoreCase(name)) {
                    state = State.PARA;
                    currentElement = new ParallelElement(currentElement);
                    seq.add(currentElement);
                } else if ("text".equalsIgnoreCase(name)) {
                    seq.add(createTextElement());
                } else if ("audio".equalsIgnoreCase(name)) {
                    seq.add(createAudioElement());
                }
                break;
            }
            case PARA: {
                log.info("para " + name);
                ParallelElement par = (ParallelElement) currentElement;
                if ("text".equalsIgnoreCase(name)) {
                    par.setTextElement(
                            createTextElement());
                } else if ("audio".equalsIgnoreCase(name)) {
                    SequenceElement seq = new SequenceElement(currentElement);
                    par.setAudioSequence(seq);
                    seq.add(createAudioElement());
                } else if ("seq".equalsIgnoreCase(name)) {
                    SequenceElement seq = createSequenceElement();
                    par.setAudioSequence(seq);
                    currentElement = seq;
                    state = State.SEQ;
                }
                break;
            }
        }
    }
    
    /**
     * Called by the SAX Parser for the end of every element.
     */
    @Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO(jharty): determine when to restore the state to the previous
    	// value. Currently we overwrite the contents of a nested parallel
    	// element because the current implementation is flawed.
    	
    	String elementName = localName != null? localName: qName;
    	
    	// OK, let's see if we can determine the parent's type (SEQ/PAR)
    	if (elementName.equalsIgnoreCase("seq") || elementName.equalsIgnoreCase("par")) {
    		ContainerElement parent = currentElement.getParent();
    		if (parent instanceof ParallelElement) {
    			log.info("End: " + elementName + ", parent element type: PAR");
    			currentElement = parent;
    			state = State.PARA;
    		} else if (parent instanceof SequenceElement) {
    			log.info("End: " + elementName + ", parent element type: SEQ");
    			currentElement = parent;
    			state = State.SEQ;
    		} else {
    			log.info("End: " + elementName);
    		}
    	}
		super.endElement(uri, localName, qName);
	}

	private SequenceElement createSequenceElement() {
        double duration = 0;
        if (attributes.getValue("dur") != null) {
            duration = Double.parseDouble(attributes.getValue("dur").replace("s", ""));
        }
        return new SequenceElement(currentElement, duration);
    }

    private AudioElement createAudioElement() {
        return new AudioElement(currentElement,
                attributes.getValue("src"),
                // TODO: support more time formats
//                Double.parseDouble(attributes.getValue("clipBegin").replace("npt=", "").replace("s", "")),
//                Double.parseDouble(attributes.getValue("clipEnd").replace("npt=", "").replace("s", "")),
                AppUtil.parseTimeToLong(attributes.getValue("clipBegin").replace("npt=", "").replace("s", "")),
                AppUtil.parseTimeToLong(attributes.getValue("clipEnd").replace("npt=", "").replace("s", "")),
                attributes.getValue("id"));
    }

    private TextElement createTextElement() {
        //TODO: handle inline text
        return new TextElement(currentElement,
                attributes.getValue("src"),
                attributes.getValue("id"));
    }
}
