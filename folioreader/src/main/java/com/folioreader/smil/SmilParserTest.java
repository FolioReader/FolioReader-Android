package com.folioreader.smil;

import java.util.List;

import junit.framework.TestCase;

public class SmilParserTest extends TestCase {
    private static final String TEXT_SRC_1 = "ncc.html#icth_0001";
    private static final String AUDIO2_MP3 = "audio2.mp3";
    private static final String AUDIO1_MP3 = "audio1.mp3";
    private SmilParser parser = new SmilParser();
    private static final String AUDIO_1 = "<audio src=\"audio1.mp3\" clip-begin=\"npt=0.000s\" clip-end=\"npt=2.00s\" id=\"audio_0001\"/>";
    private static final String AUDIO_2 = "<audio src=\"audio2.mp3\" clip-begin=\"npt=2.002s\" clip-end=\"npt=5.381s\" id=\"audio_0002\"/>";
    private static final String AUDIO_SEQ = String.format("<seq>%s</seq>", AUDIO_1 + AUDIO_2);
    private static final String SEQ_TEMPLATE = "<body><seq>%s</seq></body>";
    private static final String TEXT_1 =  "<text src=\"ncc.html#icth_0001\" id=\"icth_0001\"/>";
    private static final String TEXT_2 =  "<text src=\"ncc.html#icth_0002\" id=\"icth_0002\"/>";
    private static final String PARA_TEMPLATE = "<body><seq dur=\"1276.310s\"><par endsync=\"last\">%s</par></seq></body>";
    
    
    public void testTextElement() throws Exception {
        SequenceElement seq = parser.parse(String.format(PARA_TEMPLATE, TEXT_1));
        List<TextElement> textElements = seq.getAllTextElementDepthFirst();
        List<AudioElement> audioElements = seq.getAllAudioElementDepthFirst();
        assertEquals(TEXT_SRC_1, textElements.get(0).getSrc());
        assertEquals("How many elements should the example consist of?", 1, seq.size());
        assertTrue("Null should be returned when there is no audio element.", audioElements.isEmpty());
        assertTrue("The first element should be a Parallel Element.", seq.get(0) instanceof ParallelElement);
    }
    
    public void testAudioElement() throws Exception {
        SequenceElement seq = parser.parse(String.format(SEQ_TEMPLATE, AUDIO_1));
        List<TextElement> textElements = seq.getAllTextElementDepthFirst();
        List<AudioElement> audioElements = seq.getAllAudioElementDepthFirst();
        assertTrue("Null should be returned for the text source", textElements.isEmpty());
        assertEquals("Audio filename needs to be correct", AUDIO1_MP3, audioElements.get(0).getSrc());
        assertTrue("The first element should be a Audio Element.", seq.get(0) instanceof AudioElement);
    }
    
    public void testCombinedElement() throws Exception {
        SequenceElement seq = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1 + TEXT_1));
        List<TextElement> textElements = seq.getAllTextElementDepthFirst();
        List<AudioElement> audioElements = seq.getAllAudioElementDepthFirst();
        assertEquals("There should only be only one element for a parallel element", 1, seq.size());
        assertEquals("There should be an audio file source", AUDIO1_MP3, audioElements.get(0).getSrc());
        assertEquals(TEXT_SRC_1, textElements.get(0).getSrc());
    }
    
    public void testOrderOfElementsIsIdempotic() throws Exception {
        SequenceElement seqAT = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1 + TEXT_1));
        SequenceElement seqTA = parser.parse(String.format(PARA_TEMPLATE, TEXT_1 + AUDIO_1));
        List<AudioElement> audioElementsAT = seqAT.getAllAudioElementDepthFirst();
        List<AudioElement> audioElementsTA = seqTA.getAllAudioElementDepthFirst();
        assertEquals("Contents of the elements should be identical regardless" +
        		" of the order of elements in the parallel sequence",
        		seqAT, seqTA);
        assertEquals(
                "Contents of the audio source should be identical regardless" +
                " of order of the sub elements",
                audioElementsAT.get(0).getSrc(),
                audioElementsTA.get(0).getSrc());
    }
    
    public void testContentsOfNewArrayDontOverwriteExistingArray() throws Exception {
        SequenceElement seqAT = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1 + TEXT_1));
        SequenceElement seqTA = parser.parse(String.format(PARA_TEMPLATE, TEXT_2 + AUDIO_2));
        List<AudioElement> audioElementsAT = seqAT.getAllAudioElementDepthFirst();
        List<TextElement> textElementsAT = seqAT.getAllTextElementDepthFirst();
        List<AudioElement> audioElementsTA = seqTA.getAllAudioElementDepthFirst();
        List<TextElement> textElementsTA = seqTA.getAllTextElementDepthFirst();
        assertFalse("Contents of the elements should be different for distinct elements contents", seqAT.equals(seqTA));
        assertEquals("There should be an audio file source", AUDIO1_MP3, audioElementsAT.get(0).getSrc());
        assertEquals(TEXT_SRC_1, textElementsAT.get(0).getSrc());
        assertEquals("There should be an audio file source", AUDIO2_MP3, audioElementsTA.get(0).getSrc());
        assertEquals("ncc.html#icth_0002", textElementsTA.get(0).getSrc());
    }
    
    public void testGetAllAudioAttributes() throws Exception {
        SequenceElement seq = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1));
        List<AudioElement> audioElements = seq.getAllAudioElementDepthFirst();
        assertEquals(0.0, audioElements.get(0).getClipBegin());
        assertEquals(2.00, audioElements.get(0).getClipEnd());
        assertEquals("audio_0001", audioElements.get(0).getId());
        // <audio src=\"audio2.mp3\" clip-begin=\"npt=2.002s\" clip-end=\"npt=5.381s\" id=\"audio_0002\"/>
    }
    
    public void testGetCompanionText() throws Exception {
        // When the book is playing, there is a handler thread keep checking if the text should be updated
        // e.g. when audio1.mp3 plays upto 120 seconds, text  should change to "section 2 ..."
        SequenceElement seq = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1));
        List<AudioElement> audioElements = seq.getAllAudioElementDepthFirst();
        assertNull(audioElements.get(0).getCompanionTextElement());
        seq = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1 + TEXT_1));
        audioElements = seq.getAllAudioElementDepthFirst();
        assertEquals(TEXT_SRC_1, audioElements.get(0).getCompanionTextElement().getSrc());
    }
    
    public void testAudioElementInClip() throws Exception {
        SequenceElement seq = parser.parse(String.format(PARA_TEMPLATE, AUDIO_1));
        AudioElement audioElement = seq.getAllAudioElementDepthFirst().get(0);
        assertTrue(audioElement.inClip(0));
        assertTrue(audioElement.inClip(1.999));
        assertFalse(audioElement.inClip(2.0));
    }
    
}
