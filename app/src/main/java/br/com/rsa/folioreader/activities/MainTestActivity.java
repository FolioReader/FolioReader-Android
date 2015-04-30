package br.com.rsa.folioreader.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.rsa.folioreader.FolioReader;
import br.com.rsa.folioreader.R;
import br.com.rsa.folioreader.contracts.IFolioReader;

/**
 * Author: Rodrigo
 */

public class MainTestActivity extends ListActivity {

    static List<File> epubs;
    static List<String> names;
    private IFolioReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_test);

        List<String> listValues = new ArrayList<>();

        if ((epubs == null) || (epubs.size() == 0)) {
            epubs = epubList(Environment.getExternalStorageDirectory());
        }

        names = fileNames(epubs);

        ArrayAdapter<String> myAdapter = new ArrayAdapter <String>(this, R.layout.row_layout, R.id.listText, names);
        setListAdapter(myAdapter);

        reader = new FolioReader(getApplicationContext());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_test, menu);
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        reader.openBook(epubs.get(position).getPath());
    }

    /**
     * */

// TODO: hardcoded string
    // TODO: check with mimetype, not with filename extension
    private List<File> epubList(File dir) {
        List<File> res = new ArrayList<File>();
        if (dir.isDirectory()) {
            File[] f = dir.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; i++) {
                    if (f[i].isDirectory()) {
                        res.addAll(epubList(f[i]));
                    } else {
                        String lowerCasedName = f[i].getName().toLowerCase();
                        if (lowerCasedName.endsWith(".epub")) {
                            res.add(f[i]);
                        }

						/*
						 * NOTE: future
						if ((lowerCasedName.endsWith(".epub"))
								|| (lowerCasedName.endsWith(".e0"))) {
							res.add(f[i]);
						}
						*/
                    }
                }
            }
        }
        return res;
    }

    // TODO: hardcoded string
    private List<String> fileNames(List<File> files) {
        List<String> res = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            res.add(files.get(i).getName().replace(".epub", ""));
			/*
			 * NOTE: future
			res.add(files.get(i).getName().replace(".epub", "").replace(".e0", ""));
			*/
        }
        return res;
    }
}
