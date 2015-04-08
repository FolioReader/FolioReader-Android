package br.com.rsa.folioreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import br.com.rsa.folioreader.fragment.FolioReaderPageFragment;

/**
 * Created by rodrigo.almeida on 08/04/15.
 */
public class FolioReaderAdapter extends FragmentStatePagerAdapter {

    private int numPages;

    public FolioReaderAdapter(FragmentManager fm, int numPages) {
        super(fm);
        this.numPages = numPages;
    }

    @Override
    public Fragment getItem(int position) {
        return FolioReaderPageFragment.newInstace(position);
    }

    @Override
    public int getCount() {
        return numPages;
    }
}
