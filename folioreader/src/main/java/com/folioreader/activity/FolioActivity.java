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

public class FolioActivity extends AppCompatActivity implements ConfigViewCallback {

  private RecyclerView recyclerViewMenu;
  private RecyclerView recyclerViewFonts;
  private ConfigView configView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.folio_activity);
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    recyclerViewMenu = (RecyclerView) findViewById(R.id.recycler_view_menu);
    recyclerViewFonts = (RecyclerView) findViewById(R.id.recycler_view_fonts);
    configView = (ConfigView) findViewById(R.id.config_view);
    configRecyclerViews();
    configFolio();
    //forcePositionConfigView();
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
    recyclerViewFonts.setBackgroundColor(value);
  }

  private void configRecyclerViews() {
    recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
    recyclerViewFonts.setLayoutManager(new LinearLayoutManager(this));
  }

  private void configFolio() {
    configView.setConfigViewCallback(this);
  }

}
