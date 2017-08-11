package com.folioreader.ui.base;

import android.os.AsyncTask;

import com.folioreader.model.dictionary.Wikipedia;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author gautam chibde on 4/7/17.
 */

public class WikipediaTask extends AsyncTask<String, Void, Wikipedia> {

    private WikipediaCallBack callBack;

    public WikipediaTask(WikipediaCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected Wikipedia doInBackground(String... strings) {
        String strUrl = strings[0];
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

            try {
                JSONArray array = new JSONArray(stringBuilder.toString());

                if (array.length() == 4) {
                    try {
                        Wikipedia wikipedia = new Wikipedia();
                        wikipedia.setWord(array.get(0).toString());
                        JSONArray defs = (JSONArray) array.get(2);
                        wikipedia.setDefinition(defs.get(0).toString());
                        JSONArray links = (JSONArray) array.get(3);
                        wikipedia.setLink(links.get(0).toString());
                        return wikipedia;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }

                } else {
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Wikipedia wikipedia) {
        super.onPostExecute(wikipedia);
        if (wikipedia != null) {
            callBack.onWikipediaDataReceived(wikipedia);
        } else {
            callBack.onError();
        }
        cancel(true);
    }
}
