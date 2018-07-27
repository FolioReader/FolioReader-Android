package com.folioreader.loaders

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.folioreader.ui.folio.activity.SearchActivity
import com.folioreader.util.AppUtil
import org.readium.r2_streamer.model.searcher.SearchQueryResults
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SearchLoader : AsyncTaskLoader<Any?> {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchLoader::class.java.simpleName
        const val SEARCH_QUERY_KEY: String = "SEARCH_QUERY_KEY"
    }

    private var bundle: Bundle?

    constructor(context: Context, bundle: Bundle?) : super(context) {
        Log.d(LOG_TAG, "-> constructor")
        this.bundle = bundle
    }

    override fun onStartLoading() {
        super.onStartLoading()
        Log.d(LOG_TAG, "-> onStartLoading")

        forceLoad()
    }

    override fun loadInBackground(): Any? {
        Log.d(LOG_TAG, "-> loadInBackground")

        var searchUri: Uri? = bundle?.getParcelable(SearchActivity.BUNDLE_SEARCH_URI)
        val searchQuery: String? = bundle?.getString(SEARCH_QUERY_KEY)
        searchUri = searchUri?.buildUpon()
                ?.appendQueryParameter("query", searchQuery)
                ?.build()
        val searchUrl = URL(searchUri.toString())

        val urlConnection: HttpURLConnection = searchUrl.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        val inputStream: InputStream = urlConnection.inputStream
        val bufferedReader = BufferedReader(InputStreamReader(inputStream,
                AppUtil.charsetNameForURLConnection(urlConnection)))
        val stringBuilder = StringBuilder()

        bufferedReader.forEachLine {
            stringBuilder.append(it)
        }

        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper.readValue(stringBuilder.toString(), SearchQueryResults::class.java)
    }
}