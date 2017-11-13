package com.folioreader.ui.base;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.util.AppUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryTask extends AsyncTask<String, Void, Dictionary> {

    private static final String TAG = "DictionaryTask";

    private DictionaryCallBack callBack;

    public DictionaryTask(DictionaryCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Dictionary doInBackground(String... strings) {
        String strUrl = strings[0];
        try {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, AppUtil.charsetNameForURLConnection(urlConnection)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            return objectMapper.readValue(stringBuilder.toString(), Dictionary.class);
        } catch (IOException e) {
            Log.e(TAG, "DictionaryTask failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Dictionary dictionary) {
        super.onPostExecute(dictionary);
        if (dictionary != null) {
            callBack.onDictionaryDataReceived(dictionary);
        } else {
            callBack.onError();
        }
        cancel(true);
    }
}
