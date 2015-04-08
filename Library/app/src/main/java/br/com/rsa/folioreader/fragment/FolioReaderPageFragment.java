package br.com.rsa.folioreader.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.rsa.folioreader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FolioReaderPageFragment extends Fragment {


    public FolioReaderPageFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstace(int position){
        FolioReaderPageFragment fragment = new FolioReaderPageFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folio_reader_page, container, false);

        return view;
    }


}
