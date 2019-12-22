package com.folioreader.ui.base;


import android.os.AsyncTask;
import android.util.Log;

import com.folioreader.model.dictionary.VietnameseDictionary;
import com.folioreader.model.dictionary.VietnameseDictionaryResult;
import com.folioreader.model.sqlite.VietnameseDictionaryDatabaseHelper;

import java.util.ArrayList;


public class VietnameseDictionaryTask extends AsyncTask<String, Void, VietnameseDictionary> {
    private VietnameseDictionaryDatabaseHelper databaseHelper;
    private VietnameseDictionaryCallback callback;
    private ArrayList<VietnameseDictionaryResult> vietnameseDictionaryResults;
    //  private static Config config;

    public VietnameseDictionaryTask(VietnameseDictionaryCallback callback) {
        this.callback = callback;

        vietnameseDictionaryResults = new ArrayList<VietnameseDictionaryResult>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected VietnameseDictionary doInBackground(String... strings) {
        String word = strings[0];

        try {
            Log.v("ViDictionaryTask", "-> doInBackground -> database -> " + word);
            databaseHelper = VietnameseDictionaryDatabaseHelper.getInstance(callback.getContext_());
            databaseHelper.openDatabase();
            String defines = databaseHelper.getDefine(word);
            databaseHelper.closeDatabase();

            String[] define_example_List = defines.split("wdf_");
            for (String pair : define_example_List) {

                String[] pairArr = pair.split("wex_");

                String define = pairArr[0];
                Log.e("vi_Dict_Task: ",define +": "+define);
                String example = pairArr[pairArr.length - 1];
                Log.e("vi_Dict_Task: ",pairArr[pairArr.length - 1]);
                if (example.contains("NoEx"))
                    vietnameseDictionaryResults.add(new VietnameseDictionaryResult( word, define, " "));
                else
                    vietnameseDictionaryResults.add(new VietnameseDictionaryResult( word,   define, example));
            }

            return new VietnameseDictionary(vietnameseDictionaryResults);

        } catch (Exception e) {
            Log.e("ViDictionaryTask", e.toString());
        }
        return null;

    }

    @Override
    protected void onPostExecute(VietnameseDictionary vietnameseDictionary) {
        super.onPostExecute(vietnameseDictionary);
        Log.e("vi_Dict_Task: ",vietnameseDictionary.getResultsList().toString());
        if (vietnameseDictionary != null) {
            callback.onVietnameseDictionaryDataReceived(vietnameseDictionary);
        } else {
            callback.onError();
        }
        cancel(true);
    }
}
