package com.folioreader.ui.folio.activity

import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
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
import com.folioreader.ui.folio.adapter.AdapterBundle
import com.folioreader.ui.folio.adapter.ListViewType
import com.folioreader.ui.folio.adapter.OnItemClickListener
import com.folioreader.ui.folio.adapter.SearchAdapter
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.folioreader.view.FolioSearchView
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.Exception
import java.lang.reflect.Field

class SearchActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Any?>,
        OnItemClickListener {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val SEARCH_LOADER = 101
        const val BUNDLE_SEARCH_URI = "BUNDLE_SEARCH_URI"
        const val BUNDLE_IS_SEARCH_LOADER_RUNNING = "BUNDLE_IS_SEARCH_LOADER_RUNNING"
        const val BUNDLE_SAVE_SEARCH_QUERY = "BUNDLE_SAVE_SEARCH_QUERY"
        const val BUNDLE_IS_SOFT_KEYBOARD_VISIBLE = "BUNDLE_IS_SOFT_KEYBOARD_VISIBLE"
    }

    private lateinit var searchUri: Uri
    private lateinit var searchView: FolioSearchView
    private lateinit var actionBar: ActionBar
    private var collapseButtonView: ImageButton? = null
    private lateinit var searchAdapter: SearchAdapter
    private var searchLoader: SearchLoader? = null
    private lateinit var searchBundle: Bundle
    private var savedInstanceState: Bundle? = null
    private var softKeyboardVisible: Boolean = true

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

                    collapseButtonView?.setOnClickListener {
                        Log.d(LOG_TAG, "-> onClick -> collapseButtonView")
                        moveTaskToBack(true)
                    }

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

        searchUri = intent.getParcelableExtra(BUNDLE_SEARCH_URI)

        val emptyBundle = AdapterBundle(ListViewType.INIT_VIEW)
        searchAdapter = SearchAdapter(this, emptyBundle)
        searchAdapter.onItemClickListener = this
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = searchAdapter

        searchLoader = supportLoaderManager.initLoader(SEARCH_LOADER, null, this)
                as SearchLoader
    }

    override fun onNewIntent(intent: Intent?) {
        Log.i(LOG_TAG, "-> onNewIntent")
        intent?.putExtra(BUNDLE_SEARCH_URI, searchUri)
        setIntent(intent)

        if (Intent.ACTION_SEARCH == intent?.action) {
            val query: String = intent.getStringExtra(SearchManager.QUERY)

            searchBundle = Bundle()
            searchBundle.putParcelable(BUNDLE_SEARCH_URI, searchUri)
            searchBundle.putString(SearchManager.QUERY, query)
            searchLoader = supportLoaderManager.restartLoader(SEARCH_LOADER, searchBundle, this)
                    as SearchLoader
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(LOG_TAG, "-> onSaveInstanceState")

        outState.putCharSequence(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)

        val searchLoaderRunning = if (searchLoader == null) false else searchLoader!!.isRunning()
        outState.putBoolean(BUNDLE_IS_SEARCH_LOADER_RUNNING, searchLoaderRunning)

        outState.putBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE, softKeyboardVisible)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(LOG_TAG, "-> onRestoreInstanceState")

        this.savedInstanceState = savedInstanceState

        val searchLoaderRunning = savedInstanceState.getBoolean(BUNDLE_IS_SEARCH_LOADER_RUNNING)
        if (searchLoaderRunning) {
            searchAdapter.changeAdapterBundle(AdapterBundle(ListViewType.LOADING_VIEW))
        }
    }

    override fun onBackPressed() {
        Log.d(LOG_TAG, "-> onBackPressed")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(LOG_TAG, "-> onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_search, menu!!)

        val config: Config = AppUtil.getSavedConfig(applicationContext)
        val itemSearch: MenuItem = menu.findItem(R.id.itemSearch)
        UiUtil.setColorIntToDrawable(config.themeColor, itemSearch.icon)

        searchView = itemSearch.actionView as FolioSearchView
        searchView.init(componentName, config)

        itemSearch.expandActionView()

        searchView.setQuery(savedInstanceState?.getCharSequence(BUNDLE_SAVE_SEARCH_QUERY),
                false)

        if (savedInstanceState != null) {
            softKeyboardVisible = savedInstanceState!!.getBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE)
            if (!softKeyboardVisible) {
                Log.i(LOG_TAG, "-> onRestoreInstanceState -> !softKeyboardVisible")
                AppUtil.hideKeyboard(this)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                softKeyboardVisible = false
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (TextUtils.isEmpty(newText)) {
                    Log.d(LOG_TAG, "-> onQueryTextChange -> Empty Query")
                    supportLoaderManager.restartLoader(SEARCH_LOADER, null, this@SearchActivity)

                    val intent = Intent(FolioActivity.ACTION_SEARCH_CLEAR)
                    LocalBroadcastManager.getInstance(this@SearchActivity)
                            .sendBroadcast(intent)
                }
                return false
            }
        })

        itemSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Log.d(LOG_TAG, "-> onMenuItemActionCollapse")
                moveTaskToBack(true)
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) softKeyboardVisible = true
        }

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
                searchAdapter.changeAdapterBundle(AdapterBundle(ListViewType.LOADING_VIEW))
                return SearchLoader(this, bundle)
            }

            else -> throw UnsupportedOperationException("-> Unknown id: $id in onCreateLoader")
        }
    }

    override fun onLoadFinished(loader: Loader<Any?>, data: Any?) {

        when (loader.id) {

            SEARCH_LOADER -> {
                Log.d(LOG_TAG, "-> onLoadFinished -> " + getLoaderName(loader.id))
                searchAdapter.changeAdapterBundle(data as AdapterBundle)
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

    override fun onItemClick(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                             viewHolder: RecyclerView.ViewHolder, position: Int, id: Long) {

        if (adapter is SearchAdapter) {
            if (viewHolder is SearchAdapter.NormalViewHolder) {
                Log.d(LOG_TAG, "-> onItemClick -> " + viewHolder.searchItem)

                val intent = Intent(FolioActivity.ACTION_SEARCH_ITEM_CLICK)
                intent.putExtra(FolioActivity.EXTRA_SEARCH_ITEM, viewHolder.searchItem)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                moveTaskToBack(true)
            }
        }
    }
}
