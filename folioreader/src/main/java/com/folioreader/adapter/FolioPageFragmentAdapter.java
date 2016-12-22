package com.folioreader.adapter;

import com.folioreader.fragments.FolioPageFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.SpineReference;

/**
 * Created by mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter {
    private List<SpineReference> mSpineReferences;
    private Book mBook;
    private String mEpubFileName;
    private boolean mIsSmilAvailable;

    public FolioPageFragmentAdapter(FragmentManager fm, List<SpineReference> spineReferences,
                                    Book book, String epubFilename, boolean isSmilAvilable) {
        super(fm);
        this.mSpineReferences = spineReferences;
        this.mBook = book;
        this.mEpubFileName = epubFilename;
        this.mIsSmilAvailable=isSmilAvilable;
    }

    @Override
    public Fragment getItem(int position) {
        return FolioPageFragment.newInstance(position, mBook, mEpubFileName, mIsSmilAvailable);
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }

}
