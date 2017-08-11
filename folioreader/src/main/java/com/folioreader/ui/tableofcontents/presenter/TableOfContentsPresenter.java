package com.folioreader.ui.tableofcontents.presenter;

import com.folioreader.model.TOCLinkWrapper;
import com.folioreader.ui.base.ManifestCallBack;
import com.folioreader.ui.base.ManifestTask;

import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.model.publication.link.Link;
import org.readium.r2_streamer.model.tableofcontents.TOCLink;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gautam chibde on 8/6/17.
 */

public class TableOfContentsPresenter implements ManifestCallBack {

    private TOCMvpView tocMvpView;

    public TableOfContentsPresenter(TOCMvpView tocMvpView) {
        this.tocMvpView = tocMvpView;
    }

    public void getTOCContent(String url) {
        new ManifestTask(this).execute(url);
    }

    /**
     * [RECURSIVE]
     * <p>
     * function generates list of {@link TOCLinkWrapper} of TOC list from publication manifest
     *
     * @param tocLink     table of content elements
     * @param indentation level of hierarchy of the child elements
     * @return generated {@link TOCLinkWrapper} list
     */
    private static TOCLinkWrapper createTocLinkWrapper(TOCLink tocLink, int indentation) {
        TOCLinkWrapper tocLinkWrapper = new TOCLinkWrapper(tocLink, indentation);
        if (tocLink.getTocLinks() != null && !tocLink.getTocLinks().isEmpty()) {
            for (TOCLink tocLink1 : tocLink.getTocLinks()) {
                TOCLinkWrapper tocLinkWrapper1 = createTocLinkWrapper(tocLink1, indentation + 1);
                if (tocLinkWrapper1.getIndentation() != 3) {
                    tocLinkWrapper.addChild(tocLinkWrapper1);
                }
            }
        }
        return tocLinkWrapper;
    }

    private static ArrayList<TOCLinkWrapper> createTOCFromSpine(List<Link> spine) {
        ArrayList<TOCLinkWrapper> tocLinkWrappers = new ArrayList<>();
        for (Link link : spine) {
            TOCLink tocLink = new TOCLink();
            tocLink.bookTitle = link.bookTitle;
            tocLink.href = link.href;
            tocLinkWrappers.add(new TOCLinkWrapper(tocLink, 0));
        }
        return tocLinkWrappers;
    }

    @Override
    public void onReceivePublication(EpubPublication publication) {
        if (publication != null) {
            if (publication.tableOfContents != null) {
                ArrayList<TOCLinkWrapper> tocLinkWrappers = new ArrayList<>();
                for (TOCLink tocLink : publication.tableOfContents) {
                    TOCLinkWrapper tocLinkWrapper = createTocLinkWrapper(tocLink, 0);
                    tocLinkWrappers.add(tocLinkWrapper);
                }
                tocMvpView.onLoadTOC(tocLinkWrappers);
            } else {
                tocMvpView.onLoadTOC(createTOCFromSpine(publication.spines));
            }
        } else {
            tocMvpView.onError();
        }
    }

    @Override
    public void onError() {
        tocMvpView.onError();
    }
}
