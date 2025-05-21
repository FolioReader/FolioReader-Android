package com.folioreader.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.ui.activity.FolioActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mahavir on 12/15/16.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String FOLIO_READER_ROOT = "folioreader";

    public static String saveEpubFileAndLoadLazyBook(
            final Context context,
            FolioActivity.EpubSourceType epubSourceType,
            String epubFilePath,
            int epubRawId,
            String epubFileName
    ) {
        try {

            boolean isFolderAvailable = isFolderAvailable(epubFileName, context);
            String filePath = getFolioEpubFilePath(epubSourceType, epubFilePath, epubFileName, context);

            InputStream epubInputStream;
            if (!isFolderAvailable) {
                if (epubSourceType.equals(FolioActivity.EpubSourceType.RAW)) {
                    epubInputStream = context.getResources().openRawResource(epubRawId);
                    saveTempEpubFile(filePath, epubFileName, epubInputStream, context);
                } else if (epubSourceType.equals(FolioActivity.EpubSourceType.ASSETS)) {
                    AssetManager assetManager = context.getAssets();
                    epubFilePath = epubFilePath.replaceAll(Constants.ASSET, "");
                    epubInputStream = assetManager.open(epubFilePath);
                    saveTempEpubFile(filePath, epubFileName, epubInputStream, context);
                } else {
                    filePath = epubFilePath;
                }
            }
            return filePath;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }


    private static boolean isFolderAvailable(String epubFileName, Context context) {
        File file = new File(getFolioEpubFolderPath(epubFileName, context));
        return file.exists() && file.isDirectory();
    }

    public static String getFolioEpubFolderPath(String epubFileName, Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + FOLIO_READER_ROOT + "/" + epubFileName;
    }

    public static String getFolioEpubFilePath(
            FolioActivity.EpubSourceType sourceType,
            String epubFilePath,
            String epubFileName,
            Context context
    ) {
        if (FolioActivity.EpubSourceType.DEVICE_STORAGE.equals(sourceType)) {
            return epubFilePath;
        } else {
            return getFolioEpubFolderPath(epubFileName, context) + "/" + epubFileName + ".epub";
        }
    }


    public static String getEpubFilename(
            Context context,
            FolioActivity.EpubSourceType epubSourceType,
            String epubFilePath,
            int epubRawId
    ) {
        String epubFileName;
        if (epubSourceType.equals(FolioActivity.EpubSourceType.RAW)) {
            Resources res = context.getResources();
            epubFileName = res.getResourceEntryName(epubRawId);
        } else {
            String[] temp = epubFilePath.split("/");
            epubFileName = temp[temp.length - 1];
            int fileMaxIndex = epubFileName.length();
            epubFileName = epubFileName.substring(0, fileMaxIndex - 5);
        }

        return epubFileName;
    }

    public static void saveTempEpubFile(String filePath, String fileName, InputStream inputStream, Context context) {
        try {
            OutputStream outputStream;
            File file = new File(filePath);

            if (!file.exists()) {
                File folder = new File(getFolioEpubFolderPath(fileName, context));
                folder.mkdirs();

                outputStream = new FileOutputStream(file);
                int read = 0;
                byte[] bytes = new byte[inputStream.available()];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static String getExtensionUppercase(String path) {
        if (TextUtils.isEmpty(path))
            return null;
        int lastIndexOfDot = path.lastIndexOf('.');
        if (lastIndexOfDot == -1)
            return null;
        return path.substring(lastIndexOfDot + 1).toUpperCase();
    }
}
