/*
The MIT License (MIT)

Copyright (c) 2013, V. Giacometti, M. Giuriato, B. Petrantuono

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package com.folioreader.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.folioreader.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.EpubReader;

import static com.folioreader.Constants.CHARSET_NAME;

public class EpubManipulator {
    public static final String FILE = "file://";
    public static final String TAG = EpubManipulator.class.getSimpleName();
    private Book mBook;
    private int mCurrentSpineElementIndex;
    private String mCurrentPage;
    private String[] mSpineElementPaths;
    // NOTE: currently, counting the number of XHTML pages
    private int mPageCount;
    private int mCurrentLanguage;
    private List<String> mAvailableLanguages;
    // tells whether a page has a translation available
    private List<Boolean> mTranslations;
    private String mDecompressedFolder;
    private String mPathOPF;
    private static Context mContext;
    private static String location = Environment.getExternalStorageDirectory()
            + "/folioreader/";

    private String mFileName;
    FileInputStream mFs;
    private String mActualCSS = "";
    private String[][] mAudio;

    // mBook from mFileName
    public EpubManipulator(String srcEpubFilePath, String destFolder, Context theContext)
            throws Exception {

        List<String> spineElements;
        List<SpineReference> spineList;

        if (mContext == null) {
            mContext = theContext;
        }

        this.mFs = new FileInputStream(srcEpubFilePath);
        this.mBook = (new EpubReader()).readEpub(mFs);

        this.mFileName = srcEpubFilePath;
        this.mDecompressedFolder = destFolder;

        Spine spine = mBook.getSpine();
        spineList = spine.getSpineReferences();

        this.mCurrentSpineElementIndex = 0;
        this.mCurrentLanguage = 0;

        spineElements = new ArrayList<String>();
        pages(spineList, spineElements);
        this.mPageCount = spineElements.size();

        this.mSpineElementPaths = new String[spineElements.size()];

        unzip(srcEpubFilePath, location + mDecompressedFolder);

        mPathOPF = getPathOPF(location + mDecompressedFolder);

        for (int i = 0; i < spineElements.size(); ++i) {
            // TODO: is there a robust path joiner in the java libs?
            this.mSpineElementPaths[i] = FILE + location
                    + mDecompressedFolder + "/" + mPathOPF + "/"
                    + spineElements.get(i);
        }

        if (spineElements.size() > 0) {
            goToPage(0);
        }
        createTocFile();
    }

    // mBook from already decompressed folder
    public EpubManipulator(String mFileName, String folder, int spineIndex,
                           int language, Context theContext) throws Exception {
        List<String> spineElements;
        List<SpineReference> spineList;

        if (mContext == null) {
            mContext = theContext;
        }

        this.mFs = new FileInputStream(mFileName);
        this.mBook = (new EpubReader()).readEpub(mFs);
        this.mFileName = mFileName;
        this.mDecompressedFolder = folder;

        Spine spine = mBook.getSpine();
        spineList = spine.getSpineReferences();
        this.mCurrentSpineElementIndex = spineIndex;
        this.mCurrentLanguage = language;
        spineElements = new ArrayList<String>();
        pages(spineList, spineElements);
        this.mPageCount = spineElements.size();
        this.mSpineElementPaths = new String[spineElements.size()];

        mPathOPF = getPathOPF(location + folder);

        for (int i = 0; i < spineElements.size(); ++i) {
            // TODO: is there a robust path joiner in the java libs?
            this.mSpineElementPaths[i] = FILE + location + folder + "/"
                    + mPathOPF + "/" + spineElements.get(i);
        }
        goToPage(spineIndex);
    }

    // set language from index
    public void setLanguage(int lang) throws Exception {
        if ((lang >= 0) && (lang <= this.mAvailableLanguages.size())) {
            this.mCurrentLanguage = lang;
        }
        goToPage(this.mCurrentSpineElementIndex);
    }

    // set language from an identifier string
    public void setLanguage(String lang) throws Exception {
        int i = 0;
        while ((i < this.mAvailableLanguages.size())
                && (!(this.mAvailableLanguages.get(i).equals(lang)))) {
            i++;
        }
        setLanguage(i);
    }

    // TODO: lookup table of language names from language codes
    public String[] getLanguages() {
        String[] lang = new String[mAvailableLanguages.size()];
        for (int i = 0; i < mAvailableLanguages.size(); i++) {
            lang[i] = mAvailableLanguages.get(i);
        }
        return lang;
    }

    // create parallel text mapping
    private void pages(List<SpineReference> spineList, List<String> pages) {
        int langIndex;
        String lang;
        String actualPage;

        this.mTranslations = new ArrayList<Boolean>();
        this.mAvailableLanguages = new ArrayList<String>();

        for (int i = 0; i < spineList.size(); ++i) {
            actualPage = (spineList.get(i)).getResource().getHref();
            lang = getPageLanguage(actualPage);
            if (lang.equals("")) {
                // parallel text available
                langIndex = languageIndexFromID(lang);

                if (langIndex == this.mAvailableLanguages.size())
                    this.mAvailableLanguages.add(lang);

                if (langIndex == 0) {
                    this.mTranslations.add(true);
                    pages.add(actualPage);
                }
            } else {
                // parallel text NOT available
                this.mTranslations.add(false);
                pages.add(actualPage);
            }
        }
    }

    // language index from language string (id)
    private int languageIndexFromID(String id) {
        int i = 0;
        while ((i < mAvailableLanguages.size())
                && (!(mAvailableLanguages.get(i).equals(id)))) {
            i++;
        }
        return i;
    }

    // TODO: better parsing
    private static String getPathOPF(String unzipDir) throws IOException {
        String mPathOPF = "";
        // get the OPF path, directly from container.xml
        /*BufferedReader br = new BufferedReader(new FileReader(unzipDir
                + "/META-INF/container.xml"));*/
        BufferedReader br
                = new BufferedReader(
                new InputStreamReader(new FileInputStream(unzipDir + "/META-INF/container.xml"),
                CHARSET_NAME));
        String line;
        while ((line = br.readLine()) != null) {
            //if (line.indexOf(getS(R.string.full_path)) > -1)
            if (line.contains(getS(R.string.full_path))) {
                int start = line.indexOf(getS(R.string.full_path));
                //int start2 = line.indexOf("\"", start);
                int start2 = line.indexOf('\"', start);
                int stop2 = line.indexOf('\"', start2 + 1);
                if (start2 > -1 && stop2 > start2) {
                    mPathOPF = line.substring(start2 + 1, stop2).trim();
                    break;
                }
            }
        }
        br.close();

        // in case the OPF file is in the root directory
        if (!mPathOPF.contains("/"))
            mPathOPF = "";

        // remove the OPF file name and the preceding '/'
        int last = mPathOPF.lastIndexOf('/');
        if (last > -1) {
            mPathOPF = mPathOPF.substring(0, last);
        }

        return mPathOPF;
    }

    // TODO: more efficient unzipping
    public void unzip(String inputZip, String destinationDirectory)
            throws IOException {
        int buffer = 2048;
        List zipFiles = new ArrayList();
        File sourceZipFile = new File(inputZip);
        File unzipDestinationDirectory = new File(destinationDirectory);
        unzipDestinationDirectory.mkdir();

        ZipFile zipFile;
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        Enumeration zipFileEntries = zipFile.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {

            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(unzipDestinationDirectory, currentEntry);

            if (currentEntry.endsWith(getS(R.string.zip))) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(
                        zipFile.getInputStream(entry));
                int currentByte;
                // buffer for writing file
                byte[] data = new byte[buffer];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        buffer);

                while ((currentByte = is.read(data, 0, buffer)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();

            }

        }
        zipFile.close();

        for (Iterator iter = zipFiles.iterator(); iter.hasNext(); ) {
            String zipName = (String) iter.next();
            unzip(zipName,
                    destinationDirectory
                            + File.separatorChar
                            + zipName.substring(0,
                            zipName.lastIndexOf(getS(R.string.zip))));
        }
    }

    public void closeStream() throws IOException {
        mFs.close();
        mBook = null;
    }

    // close the stream and delete the extraction folder
    public void destroy() throws IOException {
        closeStream();
        File c = new File(location + mDecompressedFolder);
        deleteDir(c);
    }

    // recursively delete a directory
    private void deleteDir(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                deleteDir(child);
            }
        }
        f.delete();
    }

    // change the mDecompressedFolder name
    public void changeDirName(String newName) {
        File dir = new File(location + mDecompressedFolder);
        File newDir = new File(location + newName);
        dir.renameTo(newDir);

        for (int i = 0; i < mSpineElementPaths.length; ++i) {
            // TODO: is there a robust path joiner in the java libs?
            mSpineElementPaths[i] = mSpineElementPaths[i].replace(FILE
                    + location + mDecompressedFolder, FILE + location
                    + newName);
        }
        mDecompressedFolder = newName;
        try {
            goToPage(mCurrentSpineElementIndex);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage());
        }
    }

    // obtain a page in the current language
    public String goToPage(int page) throws Exception {
        return goToPage(page, this.mCurrentLanguage);
    }

    // obtain a page in the given language
    public String goToPage(int page, int lang) throws Exception {
        String spineElement;
        String extension;
        if (page < 0) {
            page = 0;
        }
        if (page >= this.mPageCount) {
            page = this.mPageCount - 1;
        }
        this.mCurrentSpineElementIndex = page;

        spineElement = this.mSpineElementPaths[mCurrentSpineElementIndex];

        // TODO: better parsing
        if (this.mTranslations.get(page)) {
            extension = spineElement.substring(spineElement.lastIndexOf('.'));
            spineElement = spineElement.substring(0,
                    spineElement.lastIndexOf(this.mAvailableLanguages.get(0)));

            spineElement = spineElement + this.mAvailableLanguages.get(lang)
                    + extension;
        }

        this.mCurrentPage = spineElement;

        audioExtractor(mCurrentPage);

        return spineElement;
    }

    public String goToNextChapter() throws Exception {
        return goToPage(this.mCurrentSpineElementIndex + 1);
    }

    public String goToPreviousChapter() throws Exception {
        return goToPage(this.mCurrentSpineElementIndex - 1);
    }

    // create an HTML page with mBook metadata
    // TODO: style it and escape metadata values
    // TODO: use StringBuilder
    public String metadata() {
        List<String> tmp;
        Metadata metadata = mBook.getMetadata();
        String html = getS(R.string.htmlBodyTableOpen);

        // Titles
        tmp = metadata.getTitles();
        if (tmp.size() > 0) {
            html += getS(R.string.titlesMeta);
            //html += "<td>" + tmp.get(0) + "</td></tr>";
            html += String.format(getS(R.string.td_tag), tmp.get(0));
            for (int i = 1; i < tmp.size(); i++) {
                //html += "<tr><td></td><td>" + tmp.get(i) + "</td></tr>";
                html += String.format(getS(R.string.tr_tag), tmp.get(i));
            }
        }

        // Authors
        List<Author> authors = metadata.getAuthors();
        if (authors.size() > 0) {
            html += getS(R.string.authorsMeta);
            html +=
                    String.format(getS(R.string.td_tag), authors.get(0).getFirstname()
                            + " " + authors.get(0).getLastname());
            for (int i = 1; i < authors.size(); i++) {
                html += String.format(getS(R.string.tr_tag),
                        authors.get(i).getFirstname() + " " + authors.get(i).getLastname());
            }
        }

        // Contributors
        authors = metadata.getContributors();
        if (authors.size() > 0) {
            html += getS(R.string.contributorsMeta);
            html += String.format(getS(R.string.td_tag),
                    authors.get(0).getFirstname() + " " + authors.get(0).getLastname());
            for (int i = 1; i < authors.size(); i++) {
                html += String.format(getS(R.string.tr_tag),
                        authors.get(i).getFirstname() + " " + authors.get(i).getLastname());
            }
        }

        // TODO: extend lib to get multiple languages?
        // Language
        html += getS(R.string.languageMeta) + metadata.getLanguage()
                + "</td></tr>";

        // Publishers
        tmp = metadata.getPublishers();
        if (tmp.size() > 0) {
            html += getS(R.string.publishersMeta);
            /*html += "<td>" + tmp.get(0) + "</td></tr>";*/
            html += String.format(getS(R.string.td_tag), tmp.get(0));
            for (int i = 1; i < tmp.size(); i++) {
                //html += "<tr><td></td><td>" + tmp.get(i) + "</td></tr>";
                html += String.format(getS(R.string.tr_tag),
                        authors.get(i).getFirstname() + " " + authors.get(i).getLastname());
            }
        }

        // Types
        tmp = metadata.getTypes();
        if (tmp.size() > 0) {
            html += getS(R.string.typesMeta);
            //html += "<td>" + tmp.get(0) + "</td></tr>";
            html += String.format(getS(R.string.td_tag), tmp.get(0));
            for (int i = 1; i < tmp.size(); i++) {
                //html += "<tr><td></td><td>" + tmp.get(i) + "</td></tr>";
                html += String.format(getS(R.string.tr_tag), tmp.get(i));
            }
        }

        // Descriptions
        tmp = metadata.getDescriptions();
        if (tmp.size() > 0) {
            html += getS(R.string.descriptionsMeta);
            //html += "<td>" + tmp.get(0) + "</td></tr>";
            html += String.format(getS(R.string.td_tag), tmp.get(0));
            for (int i = 1; i < tmp.size(); i++) {
                //html += "<tr><td></td><td>" + tmp.get(i) + "</td></tr>";
                html += String.format(getS(R.string.tr_tag), tmp.get(i));
            }
        }

        // Rights
        tmp = metadata.getRights();
        if (tmp.size() > 0) {
            html += getS(R.string.rightsMeta);
            //html += "<td>" + tmp.get(0) + "</td></tr>";
            html += String.format(getS(R.string.td_tag), tmp.get(0));
            for (int i = 1; i < tmp.size(); i++) {
                //html += "<tr><td></td><td>" + tmp.get(i) + "</td></tr>";
                html += String.format(getS(R.string.tr_tag), tmp.get(i));
            }

        }

        html += getS(R.string.tablebodyhtmlClose);
        return html;
    }

    public String createTocFile(TOCReference e) {

        String childrenPath = FILE + location + mDecompressedFolder + "/"
                + mPathOPF + "/" + e.getCompleteHref();

        String html = "<ul><li>" + "<a href=\"" + childrenPath + "\">"
                + e.getTitle() + "</a>" + "</li></ul>";

        List<TOCReference> children = e.getChildren();

        for (int j = 0; j < children.size(); j++) {
            html += createTocFile(children.get(j));
        }

        return html;
    }

    // Create an html file, which contain the TOC, in the EPUB folder
    public void createTocFile() {
        List<TOCReference> tmp;
        TableOfContents toc = mBook.getTableOfContents();
        String html = "<html><body><ul>";

        tmp = toc.getTocReferences();

        if (tmp.size() > 0) {
            html += getS(R.string.tocReference);
            for (int i = 0; i < tmp.size(); i++) {
                String path = FILE + location + mDecompressedFolder + "/"
                        + mPathOPF + "/" + tmp.get(i).getCompleteHref();

                html += "<li>" + "<a href=\"" + path + "\">"
                        + tmp.get(i).getTitle() + "</a>" + "</li>";

                // pre-order traversal?
                List<TOCReference> children = tmp.get(i).getChildren();

                for (int j = 0; j < children.size(); j++) {
                    html += createTocFile(children.get(j));
                }

            }
        }

        html += getS(R.string.tablebodyhtmlClose);

        // write down the html file
        String filePath = location + mDecompressedFolder + "/Toc.html";
        try {
            File file = new File(filePath);
            //FileWriter fw = new FileWriter(file);
            Writer w = new OutputStreamWriter(new FileOutputStream(file), CHARSET_NAME);
            PrintWriter fw = new PrintWriter(w);
            fw.write(html);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    // return the path of the Toc.html file
    public String tableOfContents() {
        return FILE + location + mDecompressedFolder + "/Toc.html";
    }

    // determine whether a mBook has the requested page
    // if so, return its index; return -1 otherwise
    public int getPageIndex(String page) {
        int result = -1;
        String lang;

        lang = getPageLanguage(page);
        if ((this.mAvailableLanguages.size() > 0) && (lang.equals(""))) {
            page = page.substring(0, page.lastIndexOf(lang))
                    + this.mAvailableLanguages.get(0)
                    + page.substring(page.lastIndexOf('.'));
        }
        for (int i = 0; i < this.mSpineElementPaths.length && result == -1; i++) {
            if (page.equals(this.mSpineElementPaths[i])) {
                result = i;
            }
        }

        return result;
    }

    // set the current page and its language
    public boolean goToPage(String page) {
        int index = getPageIndex(page);
        boolean res = false;
        if (index >= 0) {
            String newLang = getPageLanguage(page);
            try {
                goToPage(index);
                if (!newLang.equals("")) {
                    setLanguage(newLang);
                }
                res = true;
            } catch (Exception e) {
                res = false;
                Log.e(getS(R.string.error_goToPage), e.getMessage());
            }
        }
        return res;
    }

    // return the language of the page according to the
    // ISO 639-1 naming convention:
    // foo.XX.html where X \in [a-z]
    // or an empty string if language not found
    public String getPageLanguage(String page) {
        String[] tmp = page.split("\\.");
        // Language XY is present if the string format is "pagename.XY.xhtml",
        // where XY are 2 non-numeric characters that identify the language
        if (tmp.length > 2) {
            String secondFromLastItem = tmp[tmp.length - 2];
            if (secondFromLastItem.matches("[a-z][a-z]")) {
                return secondFromLastItem;
            }
        }
        return "";
    }

    // TODO work in progress
    public void addCSS(String[] settings) {
        // CSS
        String css = "<style type=\"text/css\">\n";

        if (!settings[0].isEmpty()) {
            css = css + "body{color:" + settings[0] + ";}";
            css = css + "a:link{color:" + settings[0] + ";}";
        }

        if (!settings[1].isEmpty()) {
            css = css + "body {background-color:" + settings[1] + ";}";
        }

        if (!settings[2].isEmpty()) {
            css = css + "p{font-family:" + settings[2] + ";}";
        }

        if (!settings[3].isEmpty())
            css = css + "p{\n\tfont-size:" + settings[3] + "%\n}\n";

        if (!settings[4].isEmpty()) {
            css = css + "p{line-height:" + settings[4] + "em;}";
        }

        if (!settings[5].isEmpty()) {
            css = css + "p{text-align:" + settings[5] + ";}";
        }

        if (!settings[6].isEmpty()) {
            css = css + "body{margin-left:" + settings[6] + "%;}";
        }

        if (!settings[7].isEmpty()) {
            css = css + "body{margin-right:" + settings[7] + "%;}";
        }

        css = css + "</style>";

        for (int i = 0; i < mSpineElementPaths.length; i++) {
            String path = mSpineElementPaths[i].replace(FILE, "");
            String source = readPage(path);

            source = source.replace(mActualCSS + "</head>", css + "</head>");

            writePage(path, source);
        }
        mActualCSS = css;

    }

    // change from relative path (that begin with ./ or ../) to absolute path
    private void adjustAudioLinks() {
        for (int i = 0; i < mAudio.length; i++) {
            for (int j = 0; j < mAudio[i].length; j++) {
                if (mAudio[i][j].startsWith("./")) {
                    mAudio[i][j] = mCurrentPage.substring(0,
                            mCurrentPage.lastIndexOf('/'))
                            + mAudio[i][j].substring(1);
                }

                if (mAudio[i][j].startsWith("../")) {
                    String temp = mCurrentPage.substring(0,
                            mCurrentPage.lastIndexOf('/'));
                    mAudio[i][j] = temp.substring(0, temp.lastIndexOf('/'))
                            + mAudio[i][j].substring(2);
                }
            }
        }
    }

    // Extract all the src field of an mAudio tag
    private ArrayList<String> getAudioSources(String mAudioTag) {
        ArrayList<String> srcs = new ArrayList<String>();
        Pattern p = Pattern.compile("src=\"[^\"]*\"");
        Matcher m = p.matcher(mAudioTag);
        while (m.find()) {
            srcs.add(m.group().replace("src=\"", "").replace("\"", ""));
        }

        return srcs;
    }

    // Extract all mAudio tags from an xhtml page
    private ArrayList<String> getAudioTags(String page) {
        ArrayList<String> res = new ArrayList<String>();

        String source = readPage(page);

        Pattern p = Pattern.compile("<mAudio(?s).*?</mAudio>|<mAudio(?s).*?/>");
        Matcher m = p.matcher(source);
        while (m.find()) {
            res.add(m.group(0));
        }

        return res;
    }

    private void audioExtractor(String page) {
        ArrayList<String> tags = getAudioTags(page.replace(FILE, ""));
        ArrayList<String> srcs;
        mAudio = new String[tags.size()][];

        for (int i = 0; i < tags.size(); i++) {
            srcs = getAudioSources(tags.get(i));
            mAudio[i] = new String[srcs.size()];
            for (int j = 0; j < srcs.size(); j++) {
                mAudio[i][j] = srcs.get(j);
            }
        }
        adjustAudioLinks();
    }

    public String[][] getAudio() {
        return mAudio;

    }


    // TODO work in progress
    public static String readPage(String path) {
        try {
            FileInputStream input = new FileInputStream(path);
            byte[] fileData = new byte[input.available()];

            input.read(fileData);
            input.close();

            String xhtml = new String(fileData, Charset.forName(CHARSET_NAME));
            return xhtml;
        } catch (IOException e) {
            return "";
        }
    }

    // TODO work in progress
    private boolean writePage(String path, String xhtml) {
        try {
            File file = new File(path);
            //FileWriter fw = new FileWriter(file);
            Writer w = new OutputStreamWriter(new FileOutputStream(file), CHARSET_NAME);
            PrintWriter fw = new PrintWriter(w);
            fw.write(xhtml);
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public int getCurrentSpineElementIndex() {
        return mCurrentSpineElementIndex;
    }

    public String getSpineElementPath(int elementIndex) {
        return mSpineElementPaths[elementIndex];
    }

    public String getCurrentPageURL() {
        return mCurrentPage;
    }

    public int getCurrentLanguage() {
        return mCurrentLanguage;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getDecompressedFolder() {
        return mDecompressedFolder;
    }

    public static String getS(int id) {
        return mContext.getResources().getString(id);
    }

    public Book getEpubBook(){
        return this.mBook;
    }
}
