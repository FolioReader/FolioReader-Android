package com.folioreader.ui.activity

import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.adapter.ListViewType
import com.folioreader.ui.adapter.OnItemClickListener
import com.folioreader.ui.adapter.SearchAdapter
import com.folioreader.ui.view.FolioSearchView
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.folioreader.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.reflect.Field

class SearchActivity : AppCompatActivity(), OnItemClickListener {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val BUNDLE_SPINE_SIZE = "BUNDLE_SPINE_SIZE"
        const val BUNDLE_SEARCH_URI = "BUNDLE_SEARCH_URI"
        const val BUNDLE_SAVE_SEARCH_QUERY = "BUNDLE_SAVE_SEARCH_QUERY"
        const val BUNDLE_IS_SOFT_KEYBOARD_VISIBLE = "BUNDLE_IS_SOFT_KEYBOARD_VISIBLE"
        const val BUNDLE_FIRST_VISIBLE_ITEM_INDEX = "BUNDLE_FIRST_VISIBLE_ITEM_INDEX"
    }

    enum class ResultCode(val value: Int) {
        ITEM_SELECTED(2),
        BACK_BUTTON_PRESSED(3)
    }

    private var spineSize: Int = 0
    private lateinit var searchUri: Uri
    private lateinit var searchView: FolioSearchView
    private lateinit var actionBar: ActionBar
    private var collapseButtonView: ImageButton? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchAdapterDataBundle: Bundle
    private var savedInstanceState: Bundle? = null
    private var softKeyboardVisible: Boolean = true
    private lateinit var searchViewModel: SearchViewModel

    // To get collapseButtonView from toolbar for any click events
    private val toolbarOnLayoutChangeListener: View.OnLayoutChangeListener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?, left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
        ) {

            for (i in 0 until toolbar.childCount) {

                val view: View = toolbar.getChildAt(i)
                val contentDescription: String? = view.contentDescription as String?
                if (TextUtils.isEmpty(contentDescription))
                    continue

                if (contentDescription == "Collapse") {
                    Log.v(LOG_TAG, "-> initActionBar -> mCollapseButtonView found")
                    collapseButtonView = view as ImageButton

                    collapseButtonView?.setOnClickListener {
                        Log.v(LOG_TAG, "-> onClick -> collapseButtonView")
                        navigateBack()
                    }

                    toolbar.removeOnLayoutChangeListener(this)
                    return
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(LOG_TAG, "-> onCreate")

        val config: Config = AppUtil.getSavedConfig(this)!!
        if (config.isNightMode) {
            setTheme(R.style.FolioNightTheme)
        } else {
            setTheme(R.style.FolioDayTheme)
        }

        setContentView(R.layout.activity_search)
        init(config)
    }

    private fun init(config: Config) {
        Log.v(LOG_TAG, "-> init")

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

        spineSize = intent.getIntExtra(BUNDLE_SPINE_SIZE, 0)
        searchUri = intent.getParcelableExtra(BUNDLE_SEARCH_URI)

        searchAdapter = SearchAdapter(this)
        searchAdapter.onItemClickListener = this
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = searchAdapter

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        searchAdapterDataBundle = searchViewModel.liveAdapterDataBundle.value!!

        val bundleFromFolioActivity = intent.getBundleExtra(SearchAdapter.DATA_BUNDLE)
        if (bundleFromFolioActivity != null) {
            searchViewModel.liveAdapterDataBundle.value = bundleFromFolioActivity
            searchAdapterDataBundle = bundleFromFolioActivity
            searchAdapter.changeDataBundle(bundleFromFolioActivity)
            val position = bundleFromFolioActivity.getInt(BUNDLE_FIRST_VISIBLE_ITEM_INDEX)
            Log.d(LOG_TAG, "-> onCreate -> scroll to previous position $position")
            recyclerView.scrollToPosition(position)
        }

        searchViewModel.liveAdapterDataBundle.observe(this, Observer<Bundle> { dataBundle ->
            searchAdapterDataBundle = dataBundle
            searchAdapter.changeDataBundle(dataBundle)
        })
    }

    override fun onNewIntent(intent: Intent) {
        Log.v(LOG_TAG, "-> onNewIntent")

        if (intent.hasExtra(BUNDLE_SEARCH_URI)) {
            searchUri = intent.getParcelableExtra(BUNDLE_SEARCH_URI)
        } else {
            intent.putExtra(BUNDLE_SEARCH_URI, searchUri)
            intent.putExtra(BUNDLE_SPINE_SIZE, spineSize)
        }

        setIntent(intent)

        if (Intent.ACTION_SEARCH == intent.action)
            handleSearch()
    }

    private fun handleSearch() {
        Log.v(LOG_TAG, "-> handleSearch")

        val query: String = intent.getStringExtra(SearchManager.QUERY)
        val newDataBundle = Bundle()
        newDataBundle.putString(ListViewType.KEY, ListViewType.PAGINATION_IN_PROGRESS_VIEW.toString())
        newDataBundle.putParcelableArrayList("DATA", ArrayList<SearchLocator>())
        searchViewModel.liveAdapterDataBundle.value = newDataBundle

        searchViewModel.search(spineSize, query)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(LOG_TAG, "-> onSaveInstanceState")

        outState.putCharSequence(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
        outState.putBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE, softKeyboardVisible)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.v(LOG_TAG, "-> onRestoreInstanceState")

        this.savedInstanceState = savedInstanceState
    }

    private fun navigateBack() {
        Log.v(LOG_TAG, "-> navigateBack")

        val intent = Intent()
        searchAdapterDataBundle.putInt(
            BUNDLE_FIRST_VISIBLE_ITEM_INDEX,
            linearLayoutManager.findFirstVisibleItemPosition()
        )
        intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
        intent.putExtra(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
        setResult(ResultCode.BACK_BUTTON_PRESSED.value, intent)
        finish()
    }

    override fun onBackPressed() {
        Log.v(LOG_TAG, "-> onBackPressed")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.v(LOG_TAG, "-> onCreateOptionsMenu")
        menuInflater.inflate(R.menu.menu_search, menu!!)

        val config: Config = AppUtil.getSavedConfig(applicationContext)!!
        val itemSearch: MenuItem = menu.findItem(R.id.itemSearch)
        UiUtil.setColorIntToDrawable(config.themeColor, itemSearch.icon)

        searchView = itemSearch.actionView as FolioSearchView
        searchView.init(componentName, config)

        itemSearch.expandActionView()

        if (savedInstanceState != null) {
            searchView.setQuery(
                savedInstanceState!!.getCharSequence(BUNDLE_SAVE_SEARCH_QUERY),
                false
            )
            softKeyboardVisible = savedInstanceState!!.getBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE)
            if (!softKeyboardVisible)
                AppUtil.hideKeyboard(this)
        } else {
            val searchQuery: CharSequence? = intent.getCharSequenceExtra(BUNDLE_SAVE_SEARCH_QUERY)
            if (!TextUtils.isEmpty(searchQuery)) {
                searchView.setQuery(searchQuery, false)
                AppUtil.hideKeyboard(this)
                softKeyboardVisible = false
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
                    Log.v(LOG_TAG, "-> onQueryTextChange -> Empty Query")
                    //supportLoaderManager.restartLoader(SEARCH_LOADER, null, this@SearchActivity)
                    searchViewModel.cancelAllSearchCalls()
                    searchViewModel.init()

                    val intent = Intent(FolioActivity.ACTION_SEARCH_CLEAR)
                    LocalBroadcastManager.getInstance(this@SearchActivity).sendBroadcast(intent)
                }
                return false
            }
        })

        itemSearch.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Log.v(LOG_TAG, "-> onMenuItemActionCollapse")
                navigateBack()
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
            Log.v(LOG_TAG, "-> onOptionsItemSelected -> ${item.title}")
            //onSearchRequested()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(
        adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
        viewHolder: RecyclerView.ViewHolder, position: Int, id: Long
    ) {

        if (adapter is SearchAdapter) {
            if (viewHolder is SearchAdapter.NormalViewHolder) {
                Log.v(LOG_TAG, "-> onItemClick -> " + viewHolder.searchLocator)

                val intent = Intent()
                searchAdapterDataBundle.putInt(
                    BUNDLE_FIRST_VISIBLE_ITEM_INDEX,
                    linearLayoutManager.findFirstVisibleItemPosition()
                )
                intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
                intent.putExtra(FolioActivity.EXTRA_SEARCH_ITEM, viewHolder.searchLocator as Parcelable)
                intent.putExtra(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
                setResult(ResultCode.ITEM_SELECTED.value, intent)
                finish()
            }
        }
    }
}
