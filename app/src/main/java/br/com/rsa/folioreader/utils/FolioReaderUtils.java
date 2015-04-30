package br.com.rsa.folioreader.utils;

import android.content.Context;

/**
 * Created by rodrigo.almeida on 29/04/15.
 */
public class FolioReaderUtils {
    public static String getPath(Context context) {
        return context.getExternalFilesDir(null) + "/temp/";
    }
}
