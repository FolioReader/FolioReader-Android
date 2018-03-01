package com.folioreader.android.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by priyank on 4/1/16.
 */
public class TestFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";
    private static final String HTML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xmlns:m=\"http://www.w3.org/1998/Math/MathML\" xmlns:pls=\"http://www.w3.org/2005/01/pronunciation-lexicon\" xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" xmlns:svg=\"http://www.w3.org/2000/svg\"><head><title>The Silver Chair</title><link rel=\"stylesheet\" type=\"text/css\" href=\"docbook-epub.css\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"epubbooks.css\"/><meta name=\"generator\" content=\"DocBook XSL Stylesheets Vsnapshot_9885\"/><link rel=\"next\" href=\"ch01.xhtml\" title=\"Chapter I\"/></head><body><header/><div xml:lang=\"\" class=\"book\" title=\"The Silver Chair\" id=\"simple_book\"><div class=\"titlepage\"><div><div><h1 class=\"title\">The Silver Chair</h1></div><div><div class=\"author\"><h3 class=\"author\"><span class=\"firstname\">C. S.</span> <span class=\"surname\">Lewis</span></h3></div></div><div><p class=\"copyright\">Copyright © 2014 epubBooks</p></div><div><div class=\"legalnotice\" title=\"Legal Notice\" id=\"idp140724856436624\"><p>All Rights Reserved.</p><p>This publication is protected by copyright. By payment of the required fees, you have been granted the non-exclusive, non-transferable right to access and read the text of this ebook on-screen or via personal text-to-speech computer systems. No part of this text may be reproduced, transmitted, downloaded, decompiled, reverse engineered, stored in or introduced into any information storage and retrieval system, in any form or by any means, whether electronic or mechanical, now known or hereinafter invented, without the express written permission of epubBooks.</p><p>This publication is protected by copyright. By payment of the required fees, you have been granted the non-exclusive, non-transferable right to access and read the text of this ebook on-screen or via personal text-to-speech computer systems. No part of this text may be reproduced, transmitted, downloaded, decompiled, reverse engineered, stored in or introduced into any information storage and retrieval system, in any form or by any means, whether electronic or mechanical, now known or hereinafter invented, without the express written permission of epubBooks.</p>\n" +
            "<a class=\"link\" href=\"http://www.epubbooks.com\" target=\"_top\">www.epubbooks.com</a></div></div></div><hr/></div><div class=\"dedication\" title=\"Dedication\" epub:type=\"dedication\" id=\"id70364673688860\"><div class=\"titlepage\"><div><div><h1 class=\"title\">Dedication</h1></div></div></div><p>To</p><p>Nicholas Hardie</p></div></div><footer/></body></html>";

    public static TestFragment newInstance(String content) {
        TestFragment fragment = new TestFragment();
        return fragment;
    }

    private String mContent = "???";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }

        WebView webView = new WebView(getActivity());
        webView.loadData(HTML, null, null);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);
        layout.addView(webView);

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
