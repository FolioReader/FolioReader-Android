package com.folioreader.model;

import com.folioreader.util.MultiLevelExpIndListAdapter;
import org.readium.r2.shared.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahavir on 3/10/17.
 */

public class TOCLinkWrapper implements MultiLevelExpIndListAdapter.ExpIndData {
    private Link tocLink;
    private int indentation;
    private ArrayList<TOCLinkWrapper> tocLinkWrappers;
    private boolean mIsGroup;
    private int mGroupSize;

    public TOCLinkWrapper(Link tocLink, int indentation) {
        this.tocLink = tocLink;
        this.indentation = indentation;
        this.tocLinkWrappers = new ArrayList<>();
        this.mIsGroup = (tocLink.getChildren().size() > 0);
    }

    @Override
    public String toString() {
        return "TOCLinkWrapper{" +
                "tocLink=" + tocLink +
                ", indentation=" + indentation +
                ", tocLinkWrappers=" + tocLinkWrappers +
                ", mIsGroup=" + mIsGroup +
                ", mGroupSize=" + mGroupSize +
                '}';
    }

    public int getIndentation() {
        return indentation;
    }

    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    public Link getTocLink() {
        return tocLink;
    }

    public void setTocLink(Link tocLink) {
        this.tocLink = tocLink;
    }

    public ArrayList<TOCLinkWrapper> getTocLinkWrappers() {
        return tocLinkWrappers;
    }

    public void setTocLinkWrappers(ArrayList<TOCLinkWrapper> tocLinkWrappers) {
        this.tocLinkWrappers = tocLinkWrappers;
    }

    public void addChild(TOCLinkWrapper tocLinkWrapper) {
        getTocLinkWrappers().add(tocLinkWrapper);
        //tocLinkWrapper.setIndentation(getIndentation() + 1);
    }

    @Override
    public List<? extends MultiLevelExpIndListAdapter.ExpIndData> getChildren() {
        return tocLinkWrappers;
    }

    @Override
    public boolean isGroup() {
        return mIsGroup;
    }

    @Override
    public void setIsGroup(boolean value) {
        mIsGroup = value;
    }

    @Override
    public void setGroupSize(int groupSize) {
        mGroupSize = groupSize;
    }

    public int getGroupSize() {
        return mGroupSize;
    }
}
