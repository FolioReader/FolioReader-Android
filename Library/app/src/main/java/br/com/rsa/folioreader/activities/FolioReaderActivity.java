package br.com.rsa.folioreader.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

import br.com.rsa.folioreader.FolioReader;
import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.adapter.FolioReaderAdapter;
import br.com.rsa.folioreader.adapter.FolioReaderIndexAdapter;

public class FolioReaderActivity extends ActionBarActivity {

    private List<String> urlList;
    private RelativeLayout rootView;
    private static final String STATE_ACTIVE_VIEW_ID = "net.simonvt.menudrawer.samples.WindowSample.activeViewId";
    private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.WindowSample.menuDrawer";
    private MenuDrawer mMenuDrawer;
    private int mActiveViewId;
    private FolioReaderIndexAdapter indexAdapter;
    private ListView indexListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawer.setContentView(R.layout.activity_folio_reader);
        mMenuDrawer.setMenuView(R.layout.folioreader_view_index);
        mMenuDrawer.setDropShadowSize(3);
        mMenuDrawer.setDropShadowColor(getResources().getColor(R.color.textIndex));

        rootView = (RelativeLayout) findViewById(R.id.rootView);
        indexListView = (ListView) findViewById(R.id.folioreader_index_recycler_view);

        if (savedInstanceState != null) {
            mActiveViewId = savedInstanceState.getInt(STATE_ACTIVE_VIEW_ID);
        }


        urlList = new ArrayList<String>();
        urlList.add("http://www.google.com.br/");
        urlList.add("http://www.facebook.com.br/");
        urlList.add("http://www.globo.com/");

        indexAdapter = new FolioReaderIndexAdapter(getApplicationContext(), urlList);
        indexListView.setAdapter(indexAdapter);

        FolioReader f = new FolioReader(this);
        f.setAdapter(new FolioReaderAdapter(getSupportFragmentManager(), urlList));

        rootView.addView(f.getPager());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_folio_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        outState.putInt(STATE_ACTIVE_VIEW_ID, mActiveViewId);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }

    /*
    @Override
    public void onClick(View v) {
        mMenuDrawer.setActiveView(v);
        mContentTextView.setText("Active item: " + ((TextView) v).getText());
        mMenuDrawer.closeMenu();
        mActiveViewId = v.getId();
    }
    */
}
