package com.folioreader.ui.folio.adapter

enum class ListViewType(val value: Int) {

    UNKNOWN_VIEW(0),
    INIT_VIEW(1),
    LOADING_VIEW(2),
    NORMAL_VIEW(3),
    EMPTY_VIEW(4),
    FAILURE_VIEW(5)
}