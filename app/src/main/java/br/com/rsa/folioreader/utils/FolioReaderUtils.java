package br.com.rsa.folioreader.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import nl.siegmann.epublib.domain.SpineReference;

/**
 * Created by rodrigo.almeida on 29/04/15.
 */
public class FolioReaderUtils {

    public static final String HTML_ENCODING = "UTF-8";

    public static String getPathDownloads(Context context) {
        return context.getExternalFilesDir(null) + "/downloads/";
    }

    public static String getPathePubDec(Context context) {
        return context.getExternalFilesDir(null) + "/temp/";
    }

    public static void unzipEPub(String inputZip, String destinationDirectory) throws IOException {
        int BUFFER = 2048;
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

            if (currentEntry.endsWith(".zip")) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(
                        zipFile.getInputStream(entry));
                int currentByte;
                // buffer for writing file
                byte data[] = new byte[BUFFER];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);

                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
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
            unzipEPub(zipName, destinationDirectory + File.separatorChar + zipName.substring(0, zipName.lastIndexOf(".zip")));
        }
    }

    public static String getFilename(File file) {
        return file.getName().replace(".epub", "");
    }

    public static String getFilename(String filepath) {
        File file = new File(filepath);
        return file.getName().replace(".epub", "");
    }

    public static Boolean isDecompressed(Context context, String filePath) {
        File file = new File(filePath);

        String path = FolioReaderUtils.getPathePubDec(context) + FolioReaderUtils.getFilename(file);

        if ((new File(path)).isDirectory())
            return true;

        return false;
    }

    public static String getPathOPF(String unzipDir) throws IOException {
        String pathOPF = "";
        BufferedReader br = new BufferedReader(new FileReader(unzipDir + "/META-INF/container.xml"));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf("full-path") > -1) {
                int start = line.indexOf("full-path");
                int start2 = line.indexOf("\"", start);
                int stop2 = line.indexOf("\"", start2 + 1);
                if (start2 > -1 && stop2 > start2) {
                    pathOPF = line.substring(start2 + 1, stop2).trim();
                    break;
                }
            }
        }
        br.close();

        if (!pathOPF.contains("/"))
            pathOPF = "";

        int last = pathOPF.lastIndexOf('/');
        if (last > -1) {
            pathOPF = pathOPF.substring(0, last);
        }

        return pathOPF;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Deprecated
    public static String getStringFromFile(String filePath) {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret = null;
        try {
            fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
            //Make sure you close all streams.
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getStringFromFile(String pathUrlName, boolean encode) {
        Writer writer = null;
        try {
            InputStream is = new FileInputStream(new File(pathUrlName));

            writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader;
                if (encode) {
                    reader = new BufferedReader(new InputStreamReader(is, HTML_ENCODING));
                } else {
                    reader = new BufferedReader(new InputStreamReader(is));
                }
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static int getPositionResource(List<SpineReference> list, String url) {
        for (int i=0; i<list.size(); i++) {
            SpineReference item = list.get(i);
            if (url.endsWith(item.getResource().getHref()))
                return i;
        }
        return -1;
    }
}
