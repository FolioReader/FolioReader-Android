package com.folioreader.view

interface FolioSearchBarCallback {
    fun disableSearch()
    fun showSearch(query: String?)
    fun goNextResult()
}