/**
 * Extract the XML encoding from XML content, to help parse content correctly.
 */
package com.folioreader.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Simple Utility class to extract the XML encoding from a text file.
 *
 * @author Julian Harty
 */
public final class ExtractXMLEncoding {
    private static final int ENOUGH = 200;
    protected static final String XML_TRAILER = "\"?>";
    protected static final String EXTRACT_ENCODING_REGEX = ".*encoding=\"";
    protected static final String XML_FIRST_LINE_REGEX = "<\\?xml version=\"1\\.0\" encoding=\"(.*)\"?>";

    // Hide the constructor for this utility class.
    private ExtractXMLEncoding() {
    }

    ;

    /**
     * Helper method to extract the XML file encoding
     *
     * @param line the first line of an XML file
     * @return The value of the encoding in lower-case.
     */
    protected static String extractEncoding(String line) {
        Pattern p = Pattern.compile(EXTRACT_ENCODING_REGEX);
        String []matches = p.split(line);
        String value = matches[1];  // We want the value after encoding="
        // We don't need anything after the first " after the value
        String []cleanup = value.split("\"");
        String encoding = cleanup[0];
        return encoding.toLowerCase();
    }


    /**
     * Helper method to map an unsupported XML encoding to a similar encoding.
     * <p/>
     * Currently limited to processing windows-1252 encoding.
     *
     * @param encoding The encoding string e.g. "windows-1252"
     * @return a similar, hopefully supported encoding, where we have a
     * suitable match, else the original encoding.
     */
    public static String mapUnsupportedEncoding(String encoding) {
        if (encoding.equalsIgnoreCase("windows-1252")) {
            return "iso-8859-1";
        }
        return encoding;
    }

    /**
     * Helper method to obtain the content encoding from an file.
     *
     * @param filename file to parse
     * @return the encoding if we are able to extract and parse it, else the
     * default value expected by the expat parser, i.e. "UTF-8"
     * @throws IOException if there is a problem reading from the file.
     */
    public static String obtainEncodingStringFromFile(String filename) throws IOException {
        String encoding = "UTF-8";
        FileInputStream fis = new FileInputStream(filename);
        BufferedInputStream bis = new BufferedInputStream(fis);
        if (bis.markSupported()) {
            String line = null;
            // read the first line after setting the mark, then reset
            // before calling the parser.
            bis.mark(ENOUGH);
            DataInputStream dis = new DataInputStream(bis);
            line = dis.readLine();
            line = line.replace("'", "\"");
            if (line.matches(XML_FIRST_LINE_REGEX)) {
                encoding = extractEncoding(line);
            }
            bis.reset();
        }
        fis.close();
        bis.close();
        return encoding;
    }
}
