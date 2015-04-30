package br.com.rsa.folioreader.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import br.com.rsa.folioreader.fragments.FoliorRaderViewPagerFragment;

/**
 * Created by rodrigo.almeida on 28/04/15.
 */
public class FolioReaderPagerAdapter extends FragmentStatePagerAdapter {

    public FolioReaderPagerAdapter(FragmentManager fm, List<String> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("path", list.get(position));

        FoliorRaderViewPagerFragment fragment = new FoliorRaderViewPagerFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    private List<String> list;
}
