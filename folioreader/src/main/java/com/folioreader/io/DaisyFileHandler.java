/**
 * Abstracts File operations for DAISY content.
 * 
 * The aim is to allow transparent access to content in compressed (zip) files,
 * as well as uncompressed, individual files. It's intended to cope with all
 * the various and varied files used for Daisy content e.g. structure, SMIL,
 * audio, etc.
 * 
 * This is an experimental interface which needs to be tested.
 * 
 * TODO(jharty): decide whether we'd like to support nested zip files e.g.
 * where an entire library of zip files could be enclosed in a master zip file.
 */
package com.folioreader.io;

/**
 * Allows DAISY files to be read and processed.
 * 
 * Questions to answer:
 * 1. Should the interface return java.io.File or an encapsulated, abstracted
 * class. Returning the standard java.io.File makes things easier for callers
 * to use the results of calling classes that support this interface. However
 * they then need to import java.io.File and this increases the risk they write
 * incompatible code e.g. that uses java Files directly which would mean their
 * code would fail when it encounters contents encapsulated in a zip file.
 * 
 * Hmmm, looks like http://truezip.java.net/index.html might be exactly what I
 * am looking for :) Let's see if it's suitable for our needs and works on
 * Android.
 * 
 * Answer: No, it's not ok, see the comments in the following URL
 * http://truezip.schlichtherle.de/2011/05/09/truezip-7_0-released/
 * However version 6 of TrueZip uses Java 1.4.2 and *might* work according to
 * its creator.
 * 
 * zip4j is another candidate, but isn't transparent and doesn't act as a VFS
 * however, it might be more useful than the standard java.util.Zip
 * 
 * @author Julian Harty
 */
public interface DaisyFileHandler {

}
