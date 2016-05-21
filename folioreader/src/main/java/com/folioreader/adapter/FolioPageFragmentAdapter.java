package com.folioreader.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.folioreader.fragments.FolioPageFragment;

import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentPagerAdapter {
    private List<SpineReference> mSpineReferences;
    private Book mBook;

    public FolioPageFragmentAdapter(FragmentManager fm, List<SpineReference> spineReferences, Book book) {
        super(fm);
        this.mSpineReferences = spineReferences;
        this.mBook = book;
    }

    @Override
    public Fragment getItem(int position) {
        return FolioPageFragment.newInstance(position,mBook);
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }

}
