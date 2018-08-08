package com.folioreader.model.search

import org.readium.r2_streamer.model.searcher.SearchResult

class SearchItem : SearchResult {

    var primaryContents: String? = null
    lateinit var searchItemType: SearchItemType

    constructor() : super()
    constructor(searchIndex: Int, resource: String?, title: String?, searchQuery: String?,
                matchString: String?, textBefore: String?, textAfter: String?) :
            super(searchIndex, resource, title, searchQuery, matchString, textBefore, textAfter)

    constructor(searchResult: SearchResult) : this(searchResult.searchIndex, searchResult.resource,
            searchResult.title, searchResult.searchQuery, searchResult.matchString,
            searchResult.textBefore, searchResult.textAfter)
}