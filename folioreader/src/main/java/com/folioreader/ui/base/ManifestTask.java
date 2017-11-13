package com.folioreader.ui.base;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.util.AppUtil;

import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.tableofcontents.TOCLink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Background async task which makes API call to get Epub publication
 * manifest from server
 *
 * @author by gautam on 12/6/17.
 */

public class ManifestTask extends AsyncTask<String, Void, EpubPublication> {

    private static final String TAG = "ManifestTask";

    private ManifestCallBack manifestCallBack;

    public ManifestTask(ManifestCallBack manifestCallBack) {
        this.manifestCallBack = manifestCallBack;
    }

    @Override
    protected EpubPublication doInBackground(String... urls) {
        String strUrl = urls[0];

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
            return objectMapper.readValue(stringBuilder.toString(), EpubPublication.class);
        } catch (IOException e) {
            Log.e(TAG, "ManifestTask failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(EpubPublication publication) {
        if (publication != null) {
            //TODO can be implemented in r2-streamer?
            if (publication.tableOfContents != null) {
                for (TOCLink link : publication.tableOfContents) {
                    setBookTitle(link, publication);
                }
            }
            manifestCallBack.onReceivePublication(publication);
        } else {
            manifestCallBack.onError();
        }
        cancel(true);
    }

    private void setBookTitle(TOCLink link, EpubPublication publication) {
        for (int i = 0; i < publication.spines.size(); i++) {
            if (publication.spines.get(i).href.equals(link.href)) {
                publication.spines.get(i).bookTitle = link.bookTitle;
                return;
            }
        }
    }
}