package com.folioreader.ui.folio.adapter

class AdapterBundle {

    var listViewType: ListViewType
    var data: Any? = null

    constructor(dataViewType: ListViewType) : this(dataViewType, null)

    constructor(dataViewType: ListViewType, data: Any?) {
        this.listViewType = dataViewType
        this.data = data
    }

    override fun toString(): String {
        return "AdapterBundle(listViewType=$listViewType, data=$data)"
    }
}