/*
* Copyright (C) 2016 Pedro Paulo de Amorim
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.folioreader.activity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.folioreader.R;
import com.folioreader.adapter.SpineReferenceAdapter;
import com.folioreader.adapter.TOCAdapter;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
        FolioViewCallback {

    public static final String INTENT_EPUB_ASSET_PATH = "com.folioreader.epub_asset_path";
    private RecyclerView recyclerViewMenu, recyclerViewContent;
    private FolioView folioView;
    private ConfigView configView;
    private String mEpubAssetPath;
    private Book mBook;
    private ArrayList<TOCReference> mTocReferences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folio_activity);
        mEpubAssetPath = getIntent().getStringExtra(INTENT_EPUB_ASSET_PATH);
        loadBook();
    }

    private void loadBook() {
        AssetManager assetManager = getAssets();
        try {
            InputStream epubInputStream = assetManager.open(mEpubAssetPath);
            mBook = (new EpubReader()).readEpub(epubInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        recyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
        folioView = (FolioView) findViewById(R.id.folio_view);
        configView = (ConfigView) findViewById(R.id.config_view);
        configRecyclerViews();
        configFolio();
    }

    @Override
    public void onBackPressed() {
        if (configView.isDragViewAboveTheLimit()) {
            configView.moveToOriginalPosition();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackgroundUpdate(int value) {
        recyclerViewMenu.setBackgroundColor(value);
        folioView.setBackgroundColor(value);
    }

    @Override
    public void onShadowAlpha(float alpha) {
        folioView.updateShadowAlpha(alpha);
    }

    @Override
    public void showShadow() {
        folioView.resetView();
    }

    @Override
    public void onShadowClick() {
        configView.moveOffScreen();
    }

    private void configRecyclerViews() {
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        if (mBook != null) {
            populateTableOfContents(mBook.getTableOfContents().getTocReferences(), 0);
            TOCAdapter tocAdapter = new TOCAdapter(mTocReferences);
            recyclerViewMenu.setAdapter(tocAdapter);
        }
    }

    private void populateTableOfContents(List<TOCReference> tocReferences, int depth) {
        if (tocReferences == null) {
            return;
        }

        for (TOCReference tocReference : tocReferences) {
            mTocReferences.add(tocReference);
            populateTableOfContents(tocReference.getChildren(), depth + 1);
        }
    }

    private void configFolio() {
        recyclerViewContent = (RecyclerView) folioView.findViewById(R.id.folio_element);
        recyclerViewContent.setLayoutManager(new LinearLayoutManager(this));
        if (mBook != null) {
            SpineReferenceAdapter spineAdapter = new SpineReferenceAdapter(mBook.getSpine().getSpineReferences());
            recyclerViewContent.setAdapter(spineAdapter);
        }
        folioView.setFolioViewCallback(this);
        configView.setConfigViewCallback(this);
    }

}
