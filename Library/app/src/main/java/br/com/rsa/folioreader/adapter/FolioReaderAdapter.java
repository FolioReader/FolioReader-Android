package br.com.rsa.folioreader.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import br.com.rsa.folioreader.configuration.Constants;
import br.com.rsa.folioreader.fragment.FolioReaderPageFragment;

/**
 * Created by rodrigo.almeida on 08/04/15.
 */
public class FolioReaderAdapter extends FragmentStatePagerAdapter {

    private List<String> pathPage;

    public FolioReaderAdapter(FragmentManager fm, List<String> pathPage) {
        super(fm);
        this.pathPage = pathPage;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("url", Constants.content);
        bundle.putInt("maxIndex", pathPage.size());
        bundle.putInt("currentIndex", position);

        FolioReaderPageFragment fragment = new FolioReaderPageFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return pathPage.size();
    }
}
