package com.folioreader.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.folioreader.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslateFragment extends DialogFragment {
    private View view;
    private String[] nationList_English = {"English", "Vietnamese", "Russian", "Portuguese", "Czech","Turkish"};
     private String[] nationList_English = {"İngilizce", "Vietnamca", "Rusça", "Portekizce", "Çekce","Turkish"};
    private String[] nationList_VietNamese = {"Tiếng Anh", "Tiếng Việt", "Tiếng Nga", "Tiếng Bồ Đào Nha", "Tiếng Sec","Turkish"};
    private String[] nationList_Russian = {"Английский", "вьетнамский", "русский", "португальский", "чешский","Turkish"};
    private String[] nationList_Portuguese = {"Inglês", "vietnamita", "russo", "português", "tcheco","Turkish"};
    private String[] nationList_Czech = {"Angličtina", "vietnamština", "ruština", "portugalština", "čeština","Turkish"};
    private String[] stand_list = {"en", "vi", "ru", "pt", "cs","tr"};
    private TextView translated_word;
    private Spinner trgLang_List;
    private String source = "";
    private String mean;

    public TranslateFragment(String source) {
        this.source = source;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_translate, container, false);
        setUpView();
        return view;
    }

    private void setUpView() {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        trgLang_List = view.findViewById(R.id.to_translated_spinner);
        translated_word = view.findViewById(R.id.translate_word_mean);
        ArrayAdapter<String> spinnerArrayAdapter;
        if (getDeviceLanguage().equals("vi")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_VietNamese);
        } else if (getDeviceLanguage().equals("ru")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Russian);
        } else if (getDeviceLanguage().equals("pt")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Portuguese);
        } else if (getDeviceLanguage().equals("cs")) {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_Czech);
        } else {
            spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, nationList_English);
        }

        trgLang_List.setAdapter(spinnerArrayAdapter);
        if (getDeviceLanguage().equals("vi")) {
            trgLang_List.setSelection(1);
        } else if (getDeviceLanguage().equals("ru")) {
            trgLang_List.setSelection(2);
        } else if (getDeviceLanguage().equals("pt")) {
            trgLang_List.setSelection(3);
        } else if (getDeviceLanguage().equals("cs")) {
            trgLang_List.setSelection(4);
        } 
        } else if (getDeviceLanguage().equals("tr")) {
            trgLang_List.setSelection(5);
        } else {
            trgLang_List.setSelection(0);
        }

        trgLang_List.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                   @Override
                                                   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                       getMean(stand_list[trgLang_List.getSelectedItemPosition()]);
                                                   }

                                                   @Override
                                                   public void onNothingSelected(AdapterView<?> parent) {
                                                       getMean(stand_list[trgLang_List.getSelectedItemPosition()]);
                                                   }
                                               }
        );
    }

    private String getDeviceLanguage() {
        Locale locale = getContext().getResources().getConfiguration().locale;
        //return locale; //return vi_VN
        return locale.getLanguage(); //return vi
    }

    public boolean isInternetAvailble() {
        return isConnectingToInternet() || isConnectingToWifi();
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    private boolean isConnectingToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi != null) {
            if (mWifi.getState() == NetworkInfo.State.CONNECTED)
                return true;
        }
        return false;
    }

    private void getMean(String stand_trg) {
        if (isInternetAvailble()) {
            RequestMean requestMean = new RequestMean();
            requestMean.execute("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + stand_trg + "&dt=t&q=" + this.source);
        }
        else
        {
            translated_word.setText("No network connection!");
        }
    }

    public class RequestMean extends AsyncTask<String, Void, String> {


        public RequestMean() {

        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            builder.url(strings[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Connect failed", Toast.LENGTH_SHORT).show();
                return "!@#$";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("!@#$")) {
                translated_word.setText("No network connection!");
            } else {
                try {
                    JSONArray array_1 = new JSONArray(s);
                    String str_1 = array_1.getJSONArray(0).toString();
                    JSONArray array_2 = new JSONArray(str_1);
                    String str_2 = array_2.getJSONArray(0).toString();
                    String[] str = str_2.split(",");
                    mean = str[0].substring(2, str[0].length() - 1);
                    translated_word.setText(mean);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
