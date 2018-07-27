package com.folioreader.loaders

import android.content.Context
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader
import android.util.Log

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



        return bundle?.getString(SEARCH_QUERY_KEY)
    }
}