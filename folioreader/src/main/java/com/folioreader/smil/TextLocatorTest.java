package com.folioreader.smil;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.MediumTest;

public class TextLocatorTest extends TestCase {
    private TextLocator generalTextLocator;
    private TextLocator wipoTextLocator;
    
    public void setUp() {
        generalTextLocator = new TextLocator(new File("/sdcard/testfiles"));
        wipoTextLocator = new TextLocator(new File("/sdcard/Books/WIPO-Treaty-D202Fileset"));
    }
    
    @MediumTest
    public void testLoadFromFile() throws IOException {
        assertEquals("test1\ntest2\ntest3\n", generalTextLocator.getText("test.txt"));
    }
    
    @MediumTest
    public void testLoadFromTag() throws IOException {
        assertEquals("Hello World", generalTextLocator.getText("test.xml#001"));
        assertEquals("Test", generalTextLocator.getText("test.xml#002"));
    }
    
    @MediumTest
    public void testLoadFromWipoDaisy202Book() throws IOException {
    	// TODO: Decide how to handle tabs, new line characters, etc.
    	assertEquals("Nature and Scope of Obligations \n",
    			wipoTextLocator.getText("WIPOTreatyForVisuallyImpaired.html#id_95"));
    	/**
    	 * We need to ensure TextLocator traverses into child elements;
    	 * In the following case (id_92) the body of the text is in an 'a' element.
    	 */
    	String paragraph = "The purpose of this Treaty is to provide the " +
    		"necessary minimum flexibilities in copyright laws that are needed to ensure full and equal access to information and "+
    		"communication for persons who are visually impaired or otherwise disabled in terms of reading copyrighted works, focusing in " +
    		"particular on measures that are needed to publish and distribute works in formats that are accessible for persons who are " +
    		"blind, have low vision, or have other disabilities in reading text, in order to support their full and effective participation " +
    		"in society on an equal basis with others, and to ensure the opportunity to develop and utilize their creative, artistic and " +
    		"intellectual potential, not only for their own benefit, but also for the enrichment of society. ";
    	assertEquals(paragraph, 
    		wipoTextLocator.getText("WIPOTreatyForVisuallyImpaired.html#id_92"));
    }
}
//