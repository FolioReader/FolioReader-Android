package com.folioreader.ui.folio.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.folioreader.Constants;
import com.folioreader.ui.base.HtmlTask;
import com.folioreader.ui.base.HtmlTaskCallback;
import com.folioreader.ui.folio.fragment.FolioPageFragment;

import org.readium.r2.shared.Link;
import org.readium.r2.shared.Publication;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mahavir on 4/2/16.
 */
public class FolioPageFragmentAdapter extends FragmentStatePagerAdapter implements HtmlTaskCallback {

    private static final String LOG_TAG = FolioPageFragmentAdapter.class.getSimpleName();
    private List<Link> mSpineReferences;
    private String mEpubFileName;
    private String mBookId;
    private ArrayList<Fragment> fragments;
    private ArrayList<Fragment.SavedState> savedStateList;
    private Publication publication;
    private Map<String, Integer> chapterSizesMap = new HashMap<>();
    private List<Integer> chapterSizesSequence = new ArrayList<>();
    private List<Float> chapterPercentSequence = new ArrayList<>();
    private int totalBookSize = 0;

    public FolioPageFragmentAdapter(FragmentManager fragmentManager, List<Link> spineReferences,
                                    String epubFileName, String bookId, Publication publication) {
        super(fragmentManager);
        this.mSpineReferences = spineReferences;
        this.mEpubFileName = epubFileName;
        this.mBookId = bookId;
        this.publication = publication;
        fragments = new ArrayList<>(Arrays.asList(new Fragment[mSpineReferences.size()]));
        loadAllFragments(mSpineReferences);
    }

    private void loadAllFragments(List<Link> references) {
        for (int i = 0; i < references.size(); i++) {
            Fragment fragment = fragments.get(i);
            if (fragment == null) {
                fragment = FolioPageFragment.newInstance(i, mEpubFileName, mSpineReferences.get(i), mBookId, publication);
                fragments.set(i, fragment);
                String url = Constants.LOCALHOST + Uri.encode(mEpubFileName) + mSpineReferences.get(i).getHref();
                (new HtmlTask(this)).execute(url);
            }
        }
    }

    public void updateBookSize() {
        for (int i = 0; i < fragments.size(); i++) {
            FolioPageFragment frag = (FolioPageFragment) fragments.get(i);
            if (frag == null) {
                continue;
            }
            frag.setChapterPercent(chapterPercentSequence.get(i));
            frag.setChapterSize(chapterSizesSequence.get(i));
            frag.setTotalBookSize(totalBookSize);
        }
    }


    private void createPercentageTable() {
        for (Link chapter : mSpineReferences) {
            Integer chapterSize = chapterSizesMap.get(chapter.getHref());
            chapterSizesSequence.add(chapterSize);
        }

        for (int i = 0; i < chapterSizesSequence.size(); i++) {
            List<Integer> sizesUntilChapter = chapterSizesSequence.subList(0, i);
            float totalSizeUntilChapter = 0;
            for (int j = 0; j < sizesUntilChapter.size(); j++) {
                if (sizesUntilChapter.get(j) != null) {
                    totalSizeUntilChapter += sizesUntilChapter.get(j);
                }
            }

            chapterPercentSequence.add(totalSizeUntilChapter/totalBookSize);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.set(position, null);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.set(position, fragment);
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {

        if (mSpineReferences.size() == 0 || position < 0 || position >= mSpineReferences.size())
            return null;

        Fragment fragment = fragments.get(position);
        if (fragment == null) {
            fragment = FolioPageFragment.newInstance(position,
                    mEpubFileName, mSpineReferences.get(position), mBookId, publication);
            fragments.set(position, fragment);
            ((FolioPageFragment)fragment).setChapterPercent(chapterPercentSequence.get(position));
            ((FolioPageFragment)fragment).setChapterSize(chapterSizesSequence.get(position));
            ((FolioPageFragment)fragment).setTotalBookSize(totalBookSize);
        }
        return fragment;
    }

    public ArrayList<Fragment> getFragments() {
        return fragments;
    }

    public ArrayList<Fragment.SavedState> getSavedStateList() {

        if (savedStateList == null) {
            try {
                Field field = FragmentStatePagerAdapter.class.getDeclaredField("mSavedState");
                field.setAccessible(true);
                savedStateList = (ArrayList<Fragment.SavedState>) field.get(this);
            } catch (Exception e) {
                Log.e(LOG_TAG, "-> ", e);
            }
        }

        return savedStateList;
    }

    public static Bundle getBundleFromSavedState(Fragment.SavedState savedState) {

        Bundle bundle = null;
        try {
            Field field = Fragment.SavedState.class.getDeclaredField("mState");
            field.setAccessible(true);
            bundle = (Bundle) field.get(savedState);
        } catch (Exception e) {
            Log.v(LOG_TAG, "-> " + e);
        }
        return bundle;
    }

    @Override
    public int getCount() {
        return mSpineReferences.size();
    }

    @Override
    public void onReceiveHtml(String url, String html) {
        String key = url.replace(Constants.LOCALHOST, "");
        key = key.replace(Uri.encode(mEpubFileName), "");
        chapterSizesMap.put(key, html.length());
        if (chapterSizesMap.size() == mSpineReferences.size()) {
            totalBookSize = 0;
            for (int val :
                    chapterSizesMap.values()) {
                totalBookSize += val;
            }
            createPercentageTable();
            updateBookSize();
        }
    }

    @Override
    public void onError() {

    }
}
