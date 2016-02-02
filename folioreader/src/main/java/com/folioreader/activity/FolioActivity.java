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

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import com.folioreader.R;
import com.folioreader.view.ConfigView;
import com.folioreader.view.ConfigViewCallback;
import com.folioreader.view.FolioView;
import com.folioreader.view.FolioViewCallback;

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback,
    FolioViewCallback {

  private RecyclerView recyclerViewMenu;
  private FolioView folioView;
  private ConfigView configView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.folio_activity);
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    recyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
    folioView = (FolioView) findViewById(R.id.folio_view);
    configView = (ConfigView) findViewById(R.id.config_view);
    configRecyclerViews();
    configFolio();
  }

  @Override public void onBackPressed() {
    if(configView.isDragViewAboveTheLimit()) {
      configView.moveToOriginalPosition();
    } else {
      super.onBackPressed();
    }
  }

  @Override public void onBackgroundUpdate(int value) {
    recyclerViewMenu.setBackgroundColor(value);
    folioView.setBackgroundColor(value);
  }

  @Override public void onShadowAlpha(float alpha) {
    folioView.updateShadowAlpha(alpha);
  }

  @Override public void showShadow() {
    folioView.resetView();
  }

  @Override public void onShadowClick() {
    configView.moveOffScreen();
  }

  private void configRecyclerViews() {
    recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
  }

  private void configFolio() {
    folioView.setFolioViewCallback(this);
    configView.setConfigViewCallback(this);
  }

}
