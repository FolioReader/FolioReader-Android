package com.folioreader.ui.folio.adapter

import androidx.recyclerview.widget.RecyclerView

interface OnItemClickListener {
    fun onItemClick(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                    viewHolder: RecyclerView.ViewHolder, position: Int, id: Long)
}