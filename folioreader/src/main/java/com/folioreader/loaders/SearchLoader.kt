package com.folioreader.loaders

import android.app.SearchManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import com.folioreader.model.locators.SearchItemType
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.folio.activity.SearchActivity
import com.folioreader.ui.folio.adapter.ListViewType
import com.folioreader.ui.folio.adapter.SearchAdapter
import com.folioreader.util.AppUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.readium.r2.shared.Locator
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchLoader : AsyncTaskLoader<Any?> {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchLoader::class.java.simpleName
    }

    private var loaderBundle: Bundle?
    private var running: Boolean = false
    private var cachedDataBundle: Any? = null

    constructor(context: Context, bundle: Bundle?) : super(context) {
        Log.v(LOG_TAG, "-> constructor")
        this.loaderBundle = bundle
    }

    override fun onStartLoading() {
        running = true
        super.onStartLoading()
        Log.v(LOG_TAG, "-> onStartLoading")

        if (loaderBundle == null) {
            val dataBundle = Bundle()
            dataBundle.putString(ListViewType.KEY, ListViewType.INIT_VIEW.toString())
            deliverResult(dataBundle)
            return

        } else if (loaderBundle!!.containsKey(SearchAdapter.DATA_BUNDLE)) {
            val dataBundle = loaderBundle?.getBundle(SearchAdapter.DATA_BUNDLE)
            deliverResult(dataBundle)
            return
        }

        if (cachedDataBundle != null) {
            deliverResult(cachedDataBundle)
        } else {
            forceLoad()
        }
    }

    override fun loadInBackground(): Any? {
        Log.v(LOG_TAG, "-> loadInBackground")

        var locatorList: MutableList<Locator>? = null

        try {
            val searchUri: Uri? = loaderBundle?.getParcelable(SearchActivity.BUNDLE_SEARCH_URI)
            val searchQuery: String? = loaderBundle?.getString(SearchManager.QUERY)
            val searchQueryEncoded: String? = URLEncoder.encode(searchQuery, "UTF-8")
            val searchUrl = URL(searchUri.toString() + "?query=" + searchQueryEncoded)

            val urlConnection: HttpURLConnection = searchUrl.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            val inputStream: InputStream = urlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream,
                    AppUtil.charsetNameForURLConnection(urlConnection)) as Reader?)
            val stringBuilder = StringBuilder()

            bufferedReader.forEachLine {
                stringBuilder.append(it)
            }

            //Thread.sleep(6000)

            val locatorType = object : TypeToken<MutableList<Locator>>() {}.type
            locatorList = Gson().fromJson(stringBuilder.toString(), locatorType)
            Log.d(LOG_TAG, "-> loadInBackground -> " + stringBuilder.toString())

            inputStream.close()
            urlConnection.disconnect()

        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
        }

        return when {
            locatorList == null -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.FAILURE_VIEW.toString())
                dataBundle
            }
            locatorList.size == 0 -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.EMPTY_VIEW.toString())
                dataBundle
            }
            else -> initSearchLocatorList(locatorList)
        }
    }

    override fun deliverResult(data: Any?) {
        Log.v(LOG_TAG, "-> deliverResult")

        cachedDataBundle = data
        super.deliverResult(data)

        running = false
    }

    fun isRunning(): Boolean {
        return running
    }

    private fun initSearchLocatorList(locatorList: MutableList<Locator>): Bundle {
        Log.v(LOG_TAG, "-> initSearchLocatorList")

        val searchLocatorList: MutableList<SearchLocator> = mutableListOf()

        val searchCountItem = SearchLocator()
        searchCountItem.searchItemType = SearchItemType.SEARCH_COUNT_ITEM
        searchCountItem.primaryContents = locatorList.size.toString()
        searchLocatorList.add(searchCountItem)

        var resourceHref: String? = null

        for (locator in locatorList) {

            if (resourceHref != locator.href) {
                resourceHref = locator.href
                val titleLocator = SearchLocator()
                titleLocator.searchItemType = SearchItemType.RESOURCE_TITLE_ITEM
                titleLocator.primaryContents = locator.title
                searchLocatorList.add(titleLocator)
            }

            val primaryContents = StringBuilder()
                    .append(locator.text?.before ?: "")
                    .append(locator.text?.hightlight ?: "")
                    .append(locator.text?.after ?: "")
                    .toString()
            val searchResultItem = SearchLocator(locator, primaryContents,
                    SearchItemType.SEARCH_RESULT_ITEM)
            searchLocatorList.add(searchResultItem)
        }

        val dataBundle = Bundle()
        dataBundle.putString(ListViewType.KEY, ListViewType.NORMAL_VIEW.toString())
        dataBundle.putParcelableArrayList("DATA", ArrayList(searchLocatorList))
        return dataBundle
    }
}