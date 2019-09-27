package com.folioreader.ui.base;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.folioreader.util.AppUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Background async task which downloads the html content of a web page
 * from server
 *
 * @author by gautam on 12/6/17.
 */

public class HtmlTask extends AsyncTask<String, Void, Pair<String, String>> {

    private static final String TAG = "HtmlTask";

    private HtmlTaskCallback callback;

    public HtmlTask(HtmlTaskCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Pair<String, String> doInBackground(String... urls) {
        String strUrl = urls[0];
        try {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, AppUtil.charsetNameForURLConnection(urlConnection)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            if (stringBuilder.length() > 0)
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return new Pair<>(strUrl, stringBuilder.toString());
        } catch (IOException e) {
            Log.e(TAG, "HtmlTask failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Pair<String, String> htmlPair) {
        if (htmlPair.second != null) {
            callback.onReceiveHtml(htmlPair.first, htmlPair.second);
        } else {
            callback.onError();
        }
        cancel(true);
    }
}
