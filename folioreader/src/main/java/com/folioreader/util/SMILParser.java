package com.folioreader.util;

import com.folioreader.model.media_overlay.OverlayItems;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 * @author gautam chibde on 20/6/17.
 */

public final class SMILParser {

    /**
     * Function creates list {@link OverlayItems} of all tag elements from the
     * input html raw string.
     *
     * @param html raw html string
     * @return list of {@link OverlayItems}
     */
//    public static List<OverlayItems> parseSMIL(String html) {
//        List<OverlayItems> mediaItems = new ArrayList<>();
//        try {
//            Document document = EpubParser.xmlParser(html);
//            NodeList sections = document.getDocumentElement().getElementsByTagName("section");
//            for (int i = 0; i < sections.getLength(); i++) {
//                parseNodes(mediaItems, (Element) sections.item(i));
//            }
//        } catch (Exception e) {
//            return new ArrayList<>();
//        }
//        return mediaItems;
//    }

    /**
     * [RECURSIVE]
     * Function recursively finds and parses the child elements of the input
     * DOM element.
     *
     * @param names   input {@link OverlayItems} where data is to be stored
     * @param section input DOM element
     */
    private static void parseNodes(List<OverlayItems> names, Element section) {
        for (Node n = section.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                if (e.hasAttribute("id")) {
                    names.add(new OverlayItems(e.getAttribute("id"), e.getTagName()));
                } else {
                    parseNodes(names, e);
                }
            }
        }
    }

    /**
     * function finds all the text content inside input html page and splits each sentence
     * with separator '.' and returns them as a list of {@link OverlayItems}
     *
     * @param html input raw html
     * @return generated {@link OverlayItems}
     */
//    public static List<OverlayItems> parseSMILForTTS(String html) {
//        List<OverlayItems> mediaItems = new ArrayList<>();
//        try {
//            Document document = null; //EpubParser.xmlParser(html);
//            NodeList sections = document.getDocumentElement().getElementsByTagName("body");
//            for (int i = 0; i < sections.getLength(); i++) {
//                parseNodesTTS(mediaItems, (Element) sections.item(i));
//            }
//            //} catch (EpubParserException e) {
//        } catch (Exception e) {
//            return new ArrayList<>();
//        }
//        return mediaItems;
//    }

    /**
     * [RECURSIVE]
     * Function recursively looks for the child element with the text content and
     * adds them to the input {@link OverlayItems} list
     *
     * @param names   input {@link OverlayItems} where data is to be stored
     * @param section input DOM element
     */
    private static void parseNodesTTS(List<OverlayItems> names, Element section) {
        for (Node n = section.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                for (Node n1 = e.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
                    if (n1.getTextContent() != null) {
                        for (String s : n1.getTextContent().split("\\.")) {
                            if (!s.isEmpty()) {
                                OverlayItems i = new OverlayItems();
                                i.setText(s);
                                names.add(i);
                            }
                        }
                    }
                }
                parseNodesTTS(names, e);
            }
        }
    }
}
