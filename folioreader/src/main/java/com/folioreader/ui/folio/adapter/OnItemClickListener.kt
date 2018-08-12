package com.folioreader.ui.folio.adapter

import android.support.v7.widget.RecyclerView

interface OnItemClickListener {
    fun onItemClick(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                    viewHolder: RecyclerView.ViewHolder, position: Int, id: Long)
}