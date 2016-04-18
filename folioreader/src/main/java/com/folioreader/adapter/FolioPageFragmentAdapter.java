package com.folioreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.folioreader.fragments.FolioPageFragment;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.SpineReference;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentPagerAdapter {
    private List<SpineReference> mSpineReferences;

    public FolioPageFragmentAdapter(FragmentManager fm, List<SpineReference> spineReferences) {
        super(fm);
        this.mSpineReferences = spineReferences;
    }

    @Override
    public Fragment getItem(int position) {
        return FolioPageFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }

}
