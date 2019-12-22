package com.folioreader.ui.base;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.folioreader.model.dictionary.EnglishDictionary;
import com.folioreader.network.TLSSocketFactory;
import com.folioreader.util.AppUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author gautam chibde on 4/7/17.
 */

public class EnglishDictionaryTask extends AsyncTask<String, Void, EnglishDictionary> {

    private static final String TAG = "DictionaryTask";

    private EnglishDictionaryCallBack callBack;

    public EnglishDictionaryTask(EnglishDictionaryCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected EnglishDictionary doInBackground(String... strings) {
        String strUrl = strings[0];
        try {
            Log.v(TAG, "-> doInBackground -> url -> " + strUrl);
            URL url = new URL(strUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            if (Build.VERSION.SDK_INT <= 20)
                httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
            InputStream inputStream = httpsURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,
                    AppUtil.charsetNameForURLConnection(httpsURLConnection)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            return objectMapper.readValue(stringBuilder.toString(), EnglishDictionary.class);
        } catch (Exception e) {
            Log.e(TAG, "DictionaryTask failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(EnglishDictionary englishDictionary) {
        super.onPostExecute(englishDictionary);
        if (englishDictionary != null) {
            callBack.onEnglishDictionaryDataReceived(englishDictionary);
        } else {
            callBack.onError();
        }
        cancel(true);
    }


}
