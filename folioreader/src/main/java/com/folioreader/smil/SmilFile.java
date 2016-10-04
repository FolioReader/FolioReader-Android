package com.folioreader.smil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.folioreader.io.ExtractXMLEncoding;

public class SmilFile implements Serializable {

    private SequenceElement mElements;

    /**
     * Opens a SMIL file.
     * <p/>
     * Notes:
     * - Currently a NPE can be thrown e.g. if the file has no content. This
     * is ugly. Should we convert/wrap these exceptions into an application
     * specific Exception?
     * - Also, how about adding some basic validation for the content? e.g.
     * length, structure, etc.
     * TODO(jharty): ruminate on the above notes... Address at some point.
     *
     * @param filename
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public void load(String filename)
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
        // TODO(jharty): Add validation here?
        String encoding = ExtractXMLEncoding.obtainEncodingStringFromFile(filename);
        String alternateEncoding = ExtractXMLEncoding.mapUnsupportedEncoding(encoding);
        try {
            tryToExtractElements(filename, encoding);
        } catch (SAXException saxe) {
            tryToExtractElements(filename, alternateEncoding);
        }
        return;
    }

    /**
     * Parse the contents of the InputStream.
     * <p/>
     * The contents are expected to represent a valid SMIL file.
     *
     * @param contents representing a valid SMIL file.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void parse(InputStream contents) throws IOException,
            SAXException, ParserConfigurationException {
        // TODO 20110827 (jharty): Should we check for empty content and throw an Exception?
        parseContents("UTF-8", contents);
    }

    private void tryToExtractElements(String filename, String encoding)
            throws IOException, SAXException, ParserConfigurationException {

        FileInputStream fis = new FileInputStream(filename);
        BufferedInputStream bis = new BufferedInputStream(fis);

        try {
            parseContents(encoding, bis);
        } finally {
            fis.close();
            bis.close();
        }
    }

    /**
     * Parse the contents of an InputStream.
     * <p/>
     * The parsed content is stored internally in this class.
     *
     * @param encoding the file encoding e.g. UTF-8.
     * @param bis      the InputStream to parse.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void parseContents(String encoding, InputStream bis)
            throws IOException, SAXException, ParserConfigurationException {
        mElements = new SmilParser().parse(bis, encoding);
    }

    /**
     * @return all the audio segments extracted from a Smil File.
     * <p/>
     * Note: This approach works adequately when we want the audio in
     * isolation from any other synchronised content. It's not sufficient when
     * we want to synchronise content.
     */
    @Deprecated
    public List<AudioElement> getAudioSegments() {
        return mElements.getAllAudioElementDepthFirst();
    }

    /**
     * @return all the text segments from a Smil File.
     * <p/>
     * Note: This approach works adequately when we want the text in
     * isolation from any other synchronised content. It's not sufficient when
     * we want to synchronise content.
     */
    @Deprecated
    public List<TextElement> getTextSegments() {
        return mElements.getAllTextElementDepthFirst();
    }

    /**
     * Does this Smil file contain at least 1 audio segment?
     *
     * @return true if it has, else false.
     */
    @Deprecated
    public boolean hasAudioSegments() {
        return getAudioSegments().size() > 0;
    }

    /**
     * Does this Smil file contain at least 1 text segment?
     *
     * @return true if it has, else false.
     */
    @Deprecated
    public boolean hasTextSegments() {
        return getTextSegments().size() > 0;
    }

    /**
     * @return all the mElements for this SMIL file.
     */
    public List<SmilElement> getSegments() {
        return mElements.getElements();
    }
}
