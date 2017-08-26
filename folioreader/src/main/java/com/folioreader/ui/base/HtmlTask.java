package com.folioreader.ui.base;

import android.os.AsyncTask;

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

public class HtmlTask extends AsyncTask<String, Void, String> {

    private HtmlTaskCallback callback;

    public HtmlTask(HtmlTaskCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {
        String strUrl = urls[0];
        try {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String htmlString) {
        if (htmlString != null) {
            callback.onReceiveHtml(htmlString);
        } else {
            callback.onError();
        }
        cancel(true);
    }
}
