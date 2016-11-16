/**
 * Parser for version 1 Smil files.
 * <p/>
 * Extracts the contents of the files, including the sequence of SEQ
 * (sequential) and PAR (parallel) elements. These often contain additional
 * elements e.g. text and audio contents.
 * <p/>
 * TODO(jharty):
 * We need to determine:
 * 1. Can PAR elements be nested
 * 2. Can SEQ elements be nested
 * <p/>
 * I also need to determine whether the current mState machine is the
 * appropriate model to represent the contents of SMIL files. It's certainly
 * broken currently.
 */
package com.folioreader.smil;

import com.folioreader.DummyDtdResolver;
import com.folioreader.util.AppUtil;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Parser for SMIL files.
 * <p/>
 * TODO(jharty): Think carefully how to test the parsing for more complex
 * contents. The mState transitions may be error-prone and the code in
 * startElement in particular is of concern.
 * <p/>
 * Note: Malformed or invalid content of a SMIL file stops elements from being
 * created. At the moment no error is reported in such cases. This was
 * evidenced when I tested with a simple file containing a single audio
 * element, when no elements were generated until I had added all the
 * mAttributes correctly. We could add informational messages to the create
 * methods.
 *
 * @author jharty
 */
public class SmilParser extends DefaultHandler {

    private enum State {
        INIT,
        SEQ,
        PARA,
    }

    private static final Logger log = Logger.getLogger(SmilParser.class.getSimpleName());
    private ContainerElement mCurrentElement;
    private Attributes mAttributes;
    private State mState;
    private SequenceElement mRootSequence;

    /**
     * Parse contents that should contain the Smil File structure.
     * <p/>
     * Useful for testing the parsing.
     *
     * @param content the content to parse
     * @return Sequence of Elements.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public SequenceElement parse(String content) throws IOException,
            SAXException, ParserConfigurationException {
        return this.parse(new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8"))));
    }

    /**
     * Parses the input stream of content with a default encoding of UTF-8.
     * <p/>
     * Calls parse(InputStream stream, String encoding);
     */
    public SequenceElement parse(InputStream stream) throws IOException,
            SAXException, ParserConfigurationException {
        return parse(stream, "UTF-8");
    }

    // Many thanks to the following link which provided the final piece for me,
    // http://stackoverflow.com/questions/293728/e-is-not-correctly-parsed
    public SequenceElement parse(InputStream stream, String encoding) throws IOException,
            SAXException, ParserConfigurationException {
        mState = State.INIT;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setEntityResolver(new DummyDtdResolver());
        parser.setContentHandler(this);
        InputSource input = new InputSource(stream);
        input.setEncoding(encoding);

        parser.parse(input);
        return mRootSequence;
    }

    /**
     * Called by the SAX Parser for the start of every element discovered.
     * <p/>
     * TODO(jharty): Rework, test, and simplify.
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes mAttributes) throws SAXException {
        super.startElement(uri, localName, qName, mAttributes);
        String name = localName.length() != 0 ? localName : qName;
        this.mAttributes = mAttributes;
        switch (mState) {
            case INIT: {
                log.info("init " + name);
                if ("seq".equalsIgnoreCase(name)) {
                    mState = State.SEQ;
                    mRootSequence = createSequenceElement();
                    mCurrentElement = mRootSequence;
                }
                break;
            }
            case SEQ: {
                log.info("seq " + name);
                SequenceElement seq = (SequenceElement) mCurrentElement;
                if ("par".equalsIgnoreCase(name)) {
                    mState = State.PARA;
                    mCurrentElement = new ParallelElement(mCurrentElement);
                    seq.add(mCurrentElement);
                } else if ("text".equalsIgnoreCase(name)) {
                    seq.add(createTextElement());
                } else if ("audio".equalsIgnoreCase(name)) {
                    seq.add(createAudioElement());
                }
                break;
            }
            case PARA: {
                log.info("para " + name);
                ParallelElement par = (ParallelElement) mCurrentElement;
                if ("text".equalsIgnoreCase(name)) {
                    par.setTextElement(
                            createTextElement());
                } else if ("audio".equalsIgnoreCase(name)) {
                    SequenceElement seq = new SequenceElement(mCurrentElement);
                    par.setAudioSequence(seq);
                    seq.add(createAudioElement());
                } else if ("seq".equalsIgnoreCase(name)) {
                    SequenceElement seq = createSequenceElement();
                    par.setAudioSequence(seq);
                    mCurrentElement = seq;
                    mState = State.SEQ;
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
        // TODO(jharty): determine when to restore the mState to the previous
        // value. Currently we overwrite the contents of a nested parallel
        // element because the current implementation is flawed.

        String elementName = localName != null ? localName : qName;

        // OK, let's see if we can determine the parent's type (SEQ/PAR)
        if (elementName.equalsIgnoreCase("seq") || elementName.equalsIgnoreCase("par")) {
            ContainerElement parent = mCurrentElement.getParent();
            if (parent instanceof ParallelElement) {
                log.info("End: " + elementName + ", parent element type: PAR");
                mCurrentElement = parent;
                mState = State.PARA;
            } else if (parent instanceof SequenceElement) {
                log.info("End: " + elementName + ", parent element type: SEQ");
                mCurrentElement = parent;
                mState = State.SEQ;
            } else {
                log.info("End: " + elementName);
            }
        }
        super.endElement(uri, localName, qName);
    }

    private SequenceElement createSequenceElement() {
        double duration = 0;
        if (mAttributes.getValue("dur") != null) {
            duration = Double.parseDouble(mAttributes.getValue("dur").replace("s", ""));
        }
        return new SequenceElement(mCurrentElement, duration);
    }

    private AudioElement createAudioElement() {
        return new AudioElement(mCurrentElement,
                mAttributes.getValue("src"),
                // TODO: support more time formats
                AppUtil.parseTimeToLong(mAttributes.getValue("clipBegin")
                        .replace("npt=", "").replace("s", "")),
                AppUtil.parseTimeToLong(mAttributes.getValue("clipEnd")
                        .replace("npt=", "").replace("s", "")));
    }

    private TextElement createTextElement() {
        //TODO: handle inline text
        return new TextElement(mCurrentElement,
                mAttributes.getValue("src"));
    }
}
