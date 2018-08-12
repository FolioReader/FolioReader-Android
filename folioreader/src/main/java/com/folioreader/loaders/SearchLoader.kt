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
import com.folioreader.ui.folio.adapter.AdapterBundle
import com.folioreader.ui.folio.adapter.ListViewType
import com.folioreader.util.AppUtil
import org.readium.r2_streamer.model.searcher.SearchQueryResults
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL

class SearchLoader : AsyncTaskLoader<Any?> {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchLoader::class.java.simpleName
    }

    private var bundle: Bundle?
    private var running: Boolean = false
    private var cachedData: Any? = null

    constructor(context: Context, bundle: Bundle?) : super(context) {
        Log.d(LOG_TAG, "-> constructor")
        this.bundle = bundle
    }

    override fun onStartLoading() {
        running = true
        super.onStartLoading()
        Log.d(LOG_TAG, "-> onStartLoading")

        if (bundle == null)
            deliverResult(AdapterBundle(ListViewType.INIT_VIEW))

        if (cachedData != null) {
            deliverResult(cachedData)
        } else {
            forceLoad()
        }
    }

    override fun loadInBackground(): Any? {
        Log.d(LOG_TAG, "-> loadInBackground")

        var searchQueryResults: SearchQueryResults? = null

        try {
            var searchUri: Uri? = bundle?.getParcelable(SearchActivity.BUNDLE_SEARCH_URI)
            val searchQuery: String? = bundle?.getString(SearchManager.QUERY)
            searchUri = searchUri?.buildUpon()
                    ?.appendQueryParameter("query", searchQuery)
                    ?.build()
            val searchUrl = URL(searchUri.toString())

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
            Log.d(LOG_TAG, "-> loadInBackground -> " + stringBuilder.toString())

            inputStream.close()
            urlConnection.disconnect()

        } catch (e: Exception) {
            Log.d(LOG_TAG, "-> ", e)
        }

        return when {
            searchQueryResults == null -> AdapterBundle(ListViewType.FAILURE_VIEW)
            searchQueryResults.searchCount == 0 -> AdapterBundle(ListViewType.EMPTY_VIEW)
            else -> initSearchItemList(searchQueryResults)
        }
    }

    override fun deliverResult(data: Any?) {
        Log.d(LOG_TAG, "-> deliverResult")

        cachedData = data
        super.deliverResult(data)

        running = false
    }

    fun isRunning(): Boolean {
        return running
    }

    private fun initSearchItemList(searchQueryResults: SearchQueryResults): AdapterBundle {
        Log.d(LOG_TAG, "-> initSearchItemList")

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
            searchResultItem.primaryContents = searchResultItem.matchString
            searchItemList.add(searchResultItem)
        }

        return AdapterBundle(ListViewType.NORMAL_VIEW, searchItemList)
    }
}