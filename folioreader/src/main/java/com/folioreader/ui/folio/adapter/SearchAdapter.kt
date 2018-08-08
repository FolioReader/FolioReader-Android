package com.folioreader.ui.folio.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.folioreader.R
import com.folioreader.model.search.SearchItem
import com.folioreader.model.search.SearchItemType

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    companion object {
        @JvmField
        val LOG_TAG: String? = SearchAdapter::class.simpleName
    }

    private val context: Context
    private var listViewType: ListViewType
    private var searchItemList: ArrayList<SearchItem>? = null

    constructor(context: Context, adapterBundle: AdapterBundle) : super() {
        Log.d(LOG_TAG, "-> constructor")

        this.context = context
        listViewType = adapterBundle.listViewType
        searchItemList = adapterBundle.data as? ArrayList<SearchItem>
    }

    fun changeAdapterBundle(adapterBundle: AdapterBundle) {

        listViewType = adapterBundle.listViewType
        searchItemList = adapterBundle.data as? ArrayList<SearchItem>
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {

        return if (searchItemList == null || searchItemList!!.size == 0) {
            1
        } else {
            searchItemList!!.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return listViewType.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewHolder: ViewHolder

        when (viewType) {

            ListViewType.INIT_VIEW.value -> {

                val itemView: View = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_init, parent, false)
                viewHolder = InitViewHolder(itemView)
            }

            ListViewType.LOADING_VIEW.value -> {

                val itemView: View = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_loading, parent, false)
                viewHolder = LoadingViewHolder(itemView)
            }

            ListViewType.NORMAL_VIEW.value -> {

                val itemView: View = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_normal, parent, false)
                viewHolder = NormalViewHolder(itemView)
            }

            ListViewType.EMPTY_VIEW.value -> {

                val itemView: View = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_empty, parent, false)
                viewHolder = EmptyViewHolder(itemView)
            }

            ListViewType.FAILURE_VIEW.value -> {

                val itemView: View = LayoutInflater.from(context)
                        .inflate(R.layout.item_search_failure, parent, false)
                viewHolder = FailureViewHolder(itemView)
            }

            else -> throw UnsupportedOperationException("Unknown viewType = $viewType")
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (listViewType) {

            ListViewType.INIT_VIEW -> {
            }

            ListViewType.LOADING_VIEW -> {
            }

            ListViewType.NORMAL_VIEW -> onBindNormalViewHolder(holder as NormalViewHolder, position)

            ListViewType.EMPTY_VIEW -> {
            }

            ListViewType.FAILURE_VIEW -> {
            }
        }
    }

    private fun onBindNormalViewHolder(holder: NormalViewHolder, position: Int) {

        val searchItem: SearchItem = searchItemList!![position]

        when (searchItem.searchItemType) {

            SearchItemType.SEARCH_COUNT_ITEM -> {
                val count: Int = searchItem.primaryContents?.toInt()!!
                holder.textViewCount.text = context.resources.getQuantityString(
                        R.plurals.numberOfSearchResults, count, count)
                holder.textViewCount.visibility = View.VISIBLE
                holder.textViewTitle.visibility = View.GONE
                holder.textViewResult.visibility = View.GONE
            }

            SearchItemType.PAGE_TITLE_ITEM -> {
                holder.textViewTitle.text = searchItem.primaryContents
                holder.textViewTitle.visibility = View.VISIBLE
                holder.textViewCount.visibility = View.GONE
                holder.textViewResult.visibility = View.GONE
            }

            SearchItemType.SEARCH_RESULT_ITEM -> {

                val spannableString = SpannableString(searchItem.textBefore
                        + searchItem.searchQuery
                        + searchItem.textAfter)
                val from = searchItem.textBefore.length
                val to = from + searchItem.searchQuery.length
                spannableString.setSpan(StyleSpan(Typeface.BOLD), from, to, 0)
                holder.textViewResult.text = spannableString

                holder.textViewResult.visibility = View.VISIBLE
                holder.textViewCount.visibility = View.GONE
                holder.textViewTitle.visibility = View.GONE
            }
        }
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class InitViewHolder(itemView: View) : ViewHolder(itemView)

    class LoadingViewHolder(itemView: View) : ViewHolder(itemView)

    class NormalViewHolder(itemView: View) : ViewHolder(itemView) {
        val textViewCount: TextView = itemView.findViewById(R.id.textViewCount)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewResult: TextView = itemView.findViewById(R.id.textViewResult)
    }

    class EmptyViewHolder(itemView: View) : ViewHolder(itemView)

    class FailureViewHolder(itemView: View) : ViewHolder(itemView)
}