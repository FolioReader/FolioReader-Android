package com.folioreader.ui.base;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.util.AppUtil;


import org.readium.r2_streamer.model.searcher.SearchQueryResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchListTask extends AsyncTask<String, Void, SearchQueryResults> {

    private static final String TAG = "SearchListTask";

    private SearchListCallBack callBack;
    String strUrl;

    public SearchListTask(SearchListCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected SearchQueryResults doInBackground(String... urls) {
        strUrl = urls[0];
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            InputStream inputStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, AppUtil
                    .charsetNameForURLConnection(urlConnection)));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(stringBuilder.toString(), SearchQueryResults.class);
        } catch (IOException e) {
            Log.e(TAG, "SearchListTask IOException " + e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(SearchQueryResults results) {

        if (results != null && results.getSearchCount() > 0) {
            callBack.onReceiveSearchList(results);
        } else {
            callBack.onError();
        }
        cancel(true);
    }
}
