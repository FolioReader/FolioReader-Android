package com.folioreader.ui.folio.activity

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.folioreader.R

class SearchableActivity : AppCompatActivity() {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchableActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "-> onCreate")
        setContentView(R.layout.activity_searchable)

        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            doMySearch(query)
        }
    }

    private fun doMySearch(query: String?) {
        Log.d(LOG_TAG, "-> doMySearch -> $query")
    }
}
