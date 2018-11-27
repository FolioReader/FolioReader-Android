package com.folioreader.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.folioreader.FolioReader
import com.folioreader.model.locators.SearchItemType
import com.folioreader.model.locators.SearchLocator
import com.folioreader.network.R2StreamerApi
import com.folioreader.ui.adapter.ListViewType
import org.readium.r2.shared.Locator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchViewModel : ViewModel() {

    companion object {
        val LOG_TAG: String = SearchViewModel::class.java.simpleName
    }

    var liveAdapterDataBundle: MutableLiveData<Bundle> = MutableLiveData()
    private var r2StreamerApi: R2StreamerApi? = FolioReader.get().r2StreamerApi
    private var searchCallCount: Int = 0
    private var successSearchCallCount: Int = 0
    private var errorSearchCallCount: Int = 0
    private var searchCallList: MutableList<Call<List<Locator>>> = mutableListOf()

    init {
        init()
    }

    fun init() {
        Log.v(LOG_TAG, "-> init")

        val bundle = Bundle()
        bundle.putString(ListViewType.KEY, ListViewType.INIT_VIEW.toString())
        bundle.putParcelableArrayList("DATA", ArrayList<SearchLocator>())
        liveAdapterDataBundle.value = bundle
    }

    fun search(spineSize: Int, query: String) {
        //Log.v(LOG_TAG, "-> search")
        Log.d(LOG_TAG, "-> search -> spineSize = $spineSize, query = $query")

        cancelAllSearchCalls()

        searchCallCount = spineSize
        successSearchCallCount = 0
        errorSearchCallCount = 0

        for (i in 0 until spineSize) {
            val call = r2StreamerApi?.search(i, query)
            call?.also {
                searchCallList.add(it)
                it.enqueue(SearchApiCallback())
            }
        }
    }

    fun cancelAllSearchCalls() {
        Log.v(LOG_TAG, "-> cancelAllSearchCalls")

        searchCallList.forEach { it.cancel() }
        searchCallList.clear()
    }

    inner class SearchApiCallback : Callback<List<Locator>> {
        override fun onFailure(call: Call<List<Locator>>, t: Throwable) {
            Log.e(LOG_TAG, "-> search -> onFailure", t)

            val bundle = processSingleSearchResponse(call, null, t)
            mergeSearchResponse(bundle, call)
        }

        override fun onResponse(call: Call<List<Locator>>, response: Response<List<Locator>>) {
            Log.d(LOG_TAG, "-> search -> onResponse")

            val bundle = processSingleSearchResponse(call, response, null)
            mergeSearchResponse(bundle, call)
        }
    }

    private fun mergeSearchResponse(bundle: Bundle, call: Call<List<Locator>>) {
        Log.v(LOG_TAG, "-> mergeSearchResponse")

        if (call.isCanceled)
            return

        val responseViewType = bundle.getString(ListViewType.KEY)
        if (responseViewType == ListViewType.NORMAL_VIEW.name) {

            ++successSearchCallCount
            val responseList: MutableList<SearchLocator> = bundle.getParcelableArrayList("DATA")!!
            var liveList: MutableList<SearchLocator> = liveAdapterDataBundle.value!!.getParcelableArrayList("DATA")!!
            liveList = liveList.toMutableList()

            if (liveList.isEmpty()) {
                bundle.putString(ListViewType.KEY, ListViewType.PAGINATION_IN_PROGRESS_VIEW.toString())
                liveAdapterDataBundle.value = bundle

            } else {
                val liveSearchCountItems = liveList.first().primaryContents.toInt()
                val responseSearchCountItems = responseList.first().primaryContents.toInt()
                val totalLiveSearchCountItems = liveSearchCountItems + responseSearchCountItems
                liveList.first().primaryContents = totalLiveSearchCountItems.toString()

                responseList.removeAt(0)
                liveList.addAll(responseList)

                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.PAGINATION_IN_PROGRESS_VIEW.toString())
                dataBundle.putParcelableArrayList("DATA", ArrayList(liveList))
                liveAdapterDataBundle.value = dataBundle
            }

        } else if (responseViewType == ListViewType.FAILURE_VIEW.name) {
            ++errorSearchCallCount
        } else {
            ++successSearchCallCount
        }

        if (--searchCallCount == 0) {
            val liveList: MutableList<SearchLocator> = liveAdapterDataBundle.value!!.getParcelableArrayList("DATA")!!
            val dataBundle = Bundle()
            dataBundle.putParcelableArrayList("DATA", ArrayList(liveList))

            val liveListViewType: ListViewType = if (liveList.isEmpty() && errorSearchCallCount > 0) {
                ListViewType.FAILURE_VIEW

            } else if (liveList.isEmpty()) {
                ListViewType.EMPTY_VIEW

            } else {
                ListViewType.NORMAL_VIEW
            }

            dataBundle.putString(ListViewType.KEY, liveListViewType.toString())
            liveAdapterDataBundle.value = dataBundle
        }
    }

    private fun processSingleSearchResponse(
        call: Call<List<Locator>>,
        response: Response<List<Locator>>?,
        t: Throwable?
    ): Bundle {
        Log.d(LOG_TAG, "-> processSingleSearchResponse")

        val locatorList = response?.body()
        return when {
            locatorList == null -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.FAILURE_VIEW.toString())
                dataBundle
            }

            locatorList.isEmpty() -> {
                val dataBundle = Bundle()
                dataBundle.putString(ListViewType.KEY, ListViewType.EMPTY_VIEW.toString())
                dataBundle
            }

            else -> {
                initSearchLocatorList(locatorList as MutableList<Locator>)
            }
        }
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
            val searchResultItem = SearchLocator(
                locator, primaryContents,
                SearchItemType.SEARCH_RESULT_ITEM
            )
            searchLocatorList.add(searchResultItem)
        }

        val dataBundle = Bundle()
        dataBundle.putString(ListViewType.KEY, ListViewType.NORMAL_VIEW.toString())
        dataBundle.putParcelableArrayList("DATA", ArrayList(searchLocatorList))
        return dataBundle
    }
}