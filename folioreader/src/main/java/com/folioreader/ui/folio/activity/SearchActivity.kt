package com.folioreader.ui.folio.activity

import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.loaders.SearchLoader
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.folioreader.view.FolioSearchView
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.Exception
import java.lang.reflect.Field

class SearchActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Any?> {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val SEARCH_LOADER = 101
    }

    private lateinit var searchView: FolioSearchView
    private lateinit var actionBar: ActionBar
    private var collapseButtonView: ImageButton? = null

    // To get collapseButtonView from toolbar for any click events
    private val toolbarOnLayoutChangeListener: View.OnLayoutChangeListener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int,
                                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

            for (i in 0 until toolbar.childCount) {

                val view: View = toolbar.getChildAt(i)
                val contentDescription: String? = view.contentDescription as String?
                if (TextUtils.isEmpty(contentDescription))
                    continue

                if (contentDescription == "Collapse") {
                    Log.d(LOG_TAG, "-> initActionBar -> mCollapseButtonView found")
                    collapseButtonView = view as ImageButton
                    toolbar.removeOnLayoutChangeListener(this)
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "-> onCreate")

        val config: Config = AppUtil.getSavedConfig(this)
        if (config.isNightMode) {
            setTheme(R.style.AppNightTheme)
        } else {
            setTheme(R.style.AppDayTheme)
        }

        setContentView(R.layout.activity_search)

        init(config)

        button.setOnClickListener {

            config.isNightMode = !config.isNightMode
            AppUtil.saveConfig(this, config)

            recreate()
        }
    }

    private fun init(config: Config) {
        Log.d(LOG_TAG, "-> init")

        setSupportActionBar(toolbar)
        toolbar.addOnLayoutChangeListener(toolbarOnLayoutChangeListener)
        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)

        try {
            val fieldCollapseIcon: Field = Toolbar::class.java.getDeclaredField("mCollapseIcon")
            fieldCollapseIcon.isAccessible = true
            val collapseIcon: Drawable = fieldCollapseIcon.get(toolbar) as Drawable
            UiUtil.setColorIntToDrawable(config.themeColor, collapseIcon)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        Log.i(LOG_TAG, "-> onNewIntent")

        if (Intent.ACTION_SEARCH == intent?.action) {
            val query: String = intent.getStringExtra(SearchManager.QUERY)

            loadingView.show()
            val bundle = Bundle()
            bundle.putString(SearchLoader.SEARCH_QUERY_KEY, query)
            supportLoaderManager.restartLoader(SEARCH_LOADER, bundle, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(LOG_TAG, "-> onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_search, menu!!)

        val config: Config = AppUtil.getSavedConfig(applicationContext)
        UiUtil.setColorIntToDrawable(config.themeColor, menu.findItem(R.id.itemSearch).icon)

        searchView = menu.findItem(R.id.itemSearch)!!.actionView as FolioSearchView
        searchView.init(componentName, config)

        menu.findItem(R.id.itemSearch).expandActionView()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val itemId = item?.itemId

        if (itemId == R.id.itemSearch) {
            Log.d(LOG_TAG, "-> onOptionsItemSelected -> ${item.title}")
            //onSearchRequested()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, bundle: Bundle?): Loader<Any?> {

        when (id) {

            SEARCH_LOADER -> {
                Log.d(LOG_TAG, "-> onCreateLoader -> " + getLoaderName(id))
                return SearchLoader(this, bundle)
            }

            else -> throw UnsupportedOperationException("Unknown id: $id in onCreateLoader")
        }
    }

    override fun onLoadFinished(loader: Loader<Any?>, data: Any?) {
        Log.d(LOG_TAG, "-> onLoadFinished -> " + getLoaderName(loader.id))

        when (loader.id) {

            SEARCH_LOADER -> {
                Log.d(LOG_TAG, "-> onLoadFinished -> " + getLoaderName(loader.id) + " -> " + data)
                loadingView.hide()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Any?>) {
        Log.v(LOG_TAG, "-> onLoaderReset -> " + getLoaderName(loader.id))
    }

    private fun getLoaderName(loaderId: Int): String {
        return if (loaderId == SEARCH_LOADER)
            "SEARCH_LOADER"
        else
            "UNKNOWN_LOADER"
    }
}
