package com.folioreader.ui.folio.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.folioreader.Config;
import com.folioreader.ui.folio.activity.FolioActivity;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

import org.readium.r2_streamer.model.publication.link.Link;

import java.util.List;

/**
 * @author mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter {
    private List<Link> mSpineReferences;
    private String mEpubFileName;
    private Config mConfig;

    public FolioPageFragmentAdapter(FragmentManager fm, List<Link> spineReferences, String epubFileName, Config config) {
        super(fm);
        this.mSpineReferences = spineReferences;
        this.mEpubFileName = epubFileName;
        this.mConfig = config;
        FolioActivity.BUS.register(this);
    }

    @Override
    public Fragment getItem(int position) {
        FolioPageFragment mFolioPageFragment = FolioPageFragment.newInstance(position, mEpubFileName, mSpineReferences.get(position), mConfig);
        mFolioPageFragment.setFragmentPos(position);
        return mFolioPageFragment;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }
}
