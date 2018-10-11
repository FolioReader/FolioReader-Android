package com.folioreader.loaders

import android.app.SearchManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.folioreader.model.search.SearchItem
import com.folioreader.model.search.SearchItemType
import com.folioreader.ui.folio.activity.SearchActivity
import com.folioreader.ui.folio.adapter.ListViewType
import com.folioreader.ui.folio.adapter.SearchAdapter
import com.folioreader.util.AppUtil
import org.readium.r2.streamer.r2_streamer_java.SearchQueryResults
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

        var searchQueryResults: SearchQueryResults? = null

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

            val objectMapper = ObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            searchQueryResults = objectMapper.readValue(stringBuilder.toString(),
                    SearchQueryResults::class.java)
            Log.v(LOG_TAG, "-> loadInBackground -> " + stringBuilder.toString())

            inputStream.close()
            urlConnection.disconnect()

        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
        }

        return when {
            searchQueryResults == null -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.FAILURE_VIEW.toString())
                dataBundle
            }
            searchQueryResults.searchCount == 0 -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.EMPTY_VIEW.toString())
                dataBundle
            }
            else -> initSearchItemList(searchQueryResults)
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

    private fun initSearchItemList(searchQueryResults: SearchQueryResults): Bundle {
        Log.v(LOG_TAG, "-> initSearchItemList")

        val searchItemList = ArrayList<SearchItem>()

        val searchCountItem = SearchItem()
        searchCountItem.searchItemType = SearchItemType.SEARCH_COUNT_ITEM
        searchCountItem.primaryContents = searchQueryResults.searchCount.toString()
        searchItemList.add(searchCountItem)

        var title: String? = null

        for (searchResult in searchQueryResults.searchResultList) {

            if (title != searchResult.title) {
                title = searchResult.title
                val titleItem = SearchItem()
                titleItem.searchItemType = SearchItemType.PAGE_TITLE_ITEM
                titleItem.primaryContents = title
                searchItemList.add(titleItem)
            }

            val searchResultItem = SearchItem(searchResult)
            searchResultItem.searchItemType = SearchItemType.SEARCH_RESULT_ITEM
            searchResultItem.primaryContents = searchResultItem.sentence
            searchItemList.add(searchResultItem)
        }

        val dataBundle = Bundle()
        dataBundle.putString(ListViewType.KEY, ListViewType.NORMAL_VIEW.toString())
        dataBundle.putParcelableArrayList("DATA", searchItemList)

        return dataBundle
    }
}