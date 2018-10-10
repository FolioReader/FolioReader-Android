package com.folioreader.model.search

import android.os.Parcel
import android.os.Parcelable
import org.readium.r2.streamer.r2_streamer_java.SearchResult

class SearchItem : SearchResult, Parcelable {

    var primaryContents: String? = null
    lateinit var searchItemType: SearchItemType

    constructor(parcel: Parcel) : this() {
        searchIndex = parcel.readInt()
        href = parcel.readString()
        title = parcel.readString()
        searchQuery = parcel.readString()
        matchQuery = parcel.readString()
        sentence = parcel.readString()
        textBefore = parcel.readString()
        textAfter = parcel.readString()
        occurrenceInChapter = parcel.readInt()
        primaryContents = parcel.readString()
        searchItemType = SearchItemType.valueOf(parcel.readString()!!)
    }

    constructor() : super()
    constructor(searchIndex: Int, href: String?, title: String?,
                searchQuery: String?, matchQuery: String?, sentence: String?,
                textBefore: String?, textAfter: String?, occurrenceInChapter: Int) :
            super(searchIndex, href, title, searchQuery, matchQuery,
                    sentence, textBefore, textAfter, occurrenceInChapter)

    constructor(searchResult: SearchResult) : this(searchResult.searchIndex, searchResult.href,
            searchResult.title, searchResult.searchQuery, searchResult.matchQuery,
            searchResult.sentence, searchResult.textBefore, searchResult.textAfter,
            searchResult.occurrenceInChapter)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(searchIndex)
        parcel.writeString(href)
        parcel.writeString(title)
        parcel.writeString(searchQuery)
        parcel.writeString(matchQuery)
        parcel.writeString(sentence)
        parcel.writeString(textBefore)
        parcel.writeString(textAfter)
        parcel.writeInt(occurrenceInChapter)
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