package com.folioreader.model.search

import android.os.Parcel
import android.os.Parcelable
import org.readium.r2_streamer.model.searcher.SearchResult

class SearchItem : SearchResult, Parcelable {

    var primaryContents: String? = null
    lateinit var searchItemType: SearchItemType

    constructor(parcel: Parcel) : this() {
        searchIndex = parcel.readInt()
        href = parcel.readString()
        originalHref = parcel.readString()
        title = parcel.readString()
        searchQuery = parcel.readString()
        matchString = parcel.readString()
        textBefore = parcel.readString()
        textAfter = parcel.readString()
        primaryContents = parcel.readString()
        searchItemType = SearchItemType.valueOf(parcel.readString()!!)
    }

    constructor() : super()
    constructor(searchIndex: Int, href: String?, originalHref: String?, title: String?,
                searchQuery: String?, matchString: String?, textBefore: String?,
                textAfter: String?) :
            super(searchIndex, href, originalHref, title, searchQuery, matchString, textBefore,
                    textAfter)

    constructor(searchResult: SearchResult) : this(searchResult.searchIndex, searchResult.href,
            searchResult.originalHref, searchResult.title, searchResult.searchQuery,
            searchResult.matchString, searchResult.textBefore, searchResult.textAfter)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(searchIndex)
        parcel.writeString(href)
        parcel.writeString(originalHref)
        parcel.writeString(title)
        parcel.writeString(searchQuery)
        parcel.writeString(matchString)
        parcel.writeString(textBefore)
        parcel.writeString(textAfter)
        parcel.writeString(primaryContents)
        parcel.writeString(searchItemType.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchItem> {
        override fun createFromParcel(parcel: Parcel): SearchItem {
            return SearchItem(parcel)
        }

        override fun newArray(size: Int): Array<SearchItem?> {
            return arrayOfNulls(size)
        }
    }
}