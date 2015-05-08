package br.com.rsa.folioreader.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.adapters.FolioReaderIndexAdapter;
import br.com.rsa.folioreader.adapters.FolioReaderPagerAdapter;
import br.com.rsa.folioreader.configuration.Configuration;
import br.com.rsa.folioreader.entities.BookDecompressed;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;


public class FolioReaderActivity extends AppCompatActivity {
    private VerticalViewPager viewPager;
    private static final String STATE_ACTIVE_VIEW_ID = "net.simonvt.menudrawer.samples.WindowSample.activeViewId";
    private static final String STATE_MENUDRAWER = "net.simonvt.menudrawer.samples.WindowSample.menuDrawer";
    private int activeViewId;

    /**
     *
     * Views used in the Folio Reader;
     */
    private ListView listViewIndex;
    private BookDecompressed bookDecompressed;
    private MenuDrawer menuDrawer;
    private LinearLayout panelButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.initConfiguration(getApplicationContext());
        menuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
        menuDrawer.setContentView(R.layout.activity_folio_reader);
        menuDrawer.setMenuView(R.layout.folioreader_menudrawer_items);
        menuDrawer.setDropShadowColor(getResources().getColor(R.color.shadowIndexMenuAndSeparator));
        menuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
        init();

        if (savedInstanceState != null) {
            activeViewId = savedInstanceState.getInt(STATE_ACTIVE_VIEW_ID);
        }
        viewPager.setAdapter(new FolioReaderPagerAdapter(getSupportFragmentManager(), bookDecompressed.getUrlResources()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folio_reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        menuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, menuDrawer.saveState());
        outState.putInt(STATE_ACTIVE_VIEW_ID, activeViewId);
    }

    @Override
    public void onBackPressed() {
        final int drawerState = menuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            menuDrawer.closeMenu();
            return;
        }

        super.onBackPressed();
    }

    /**
     *
     * Init all configurations from reader;
     */
    private void init() {
        bookDecompressed = (BookDecompressed) Configuration.getData("key-book");
        viewPager = (VerticalViewPager) findViewById(R.id.folioreader_vertical_viewpager);
        listViewIndex = (ListView) findViewById(R.id.folioreader_listview_index);
        panelButtons = (LinearLayout) findViewById(R.id.folioreader_panel_buttons);

        /**
         *
         * Set Colors
         */
        listViewIndex.setBackgroundColor((int) Configuration.getData(Configuration.COLOR_LIST_INDEX));
        panelButtons.setBackgroundColor((int) Configuration.getData(Configuration.COLOR_PANEL_BUTTONS));
        ((ImageButton)findViewById(R.id.folioreader_btn_highligth)).setBackgroundColor((int) Configuration.getData(Configuration.COLOR_PANEL_BUTTONS));
        ((ImageButton)findViewById(R.id.folioreader_btn_font)).setBackgroundColor((int) Configuration.getData(Configuration.COLOR_PANEL_BUTTONS));
        ((ImageButton)findViewById(R.id.folioreader_btn_search)).setBackgroundColor((int) Configuration.getData(Configuration.COLOR_PANEL_BUTTONS));

        new CreateIndex().execute(bookDecompressed.getBook().getTableOfContents());
    }

    /**
     * *** Class AsyncTask *****
     */
    private class CreateIndex extends AsyncTask<TableOfContents, Void, List<TOCReference>> {

        private int lastPaging = 0;
        private List<TOCReference> indexes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            indexes = new ArrayList<>();
        }

        @Override
        protected List<TOCReference> doInBackground(TableOfContents... tableOfContentses) {
            for (TOCReference tocReference : tableOfContentses[0].getTocReferences()) {
                getIndexRecursive(tocReference);
            }
            return indexes;
        }

        @Override
        protected void onPostExecute(final List<TOCReference> indexes) {
            super.onPostExecute(indexes);

            listViewIndex.setAdapter(new FolioReaderIndexAdapter(getApplicationContext(), indexes));
            listViewIndex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Resource resource = indexes.get(position).getResource();

                    int moveTo = bookDecompressed.getBook().getSpine().findFirstResourceById(resource.getId());
                    viewPager.setCurrentItem(moveTo);

                    menuDrawer.closeMenu();
                }
            });
        }

        //Recursive create index
        private void getIndexRecursive(TOCReference tocReference) {
            if (tocReference != null)
                indexes.add(tocReference);

            for (TOCReference item : tocReference.getChildren()) {
                getIndexRecursive(item);
            }
        }
    }
}
