package com.folioreader.model.locators

import android.os.Parcel
import android.os.Parcelable
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator
import org.readium.r2.shared.LocatorText

enum class SearchItemType {
    UNKNOWN_ITEM,
    SEARCH_COUNT_ITEM,
    RESOURCE_TITLE_ITEM,
    SEARCH_RESULT_ITEM
}

class SearchLocator : Locator, Parcelable {

    var primaryContents: String
    var searchItemType: SearchItemType

    constructor() : this(Locator("", 0, "", Locations(), null), "", SearchItemType.UNKNOWN_ITEM)

    constructor(locator: Locator, primaryContents: String, searchItemType: SearchItemType) :
            super(locator.href, locator.created, locator.title, locator.locations, locator.text) {
        this.primaryContents = primaryContents
        this.searchItemType = searchItemType
    }

    constructor(parcel: Parcel) : this(
        Locator(
            parcel.readString()!!,
            parcel.readLong(),
            parcel.readString()!!,
            parcel.readSerializable() as Locations,
            parcel.readSerializable() as LocatorText?
        ), parcel.readString()!!, SearchItemType.valueOf(parcel.readString()!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(href)
        parcel.writeLong(created)
        parcel.writeString(title)
        parcel.writeSerializable(locations)
        parcel.writeSerializable(text)
        parcel.writeString(primaryContents)
        parcel.writeString(searchItemType.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<SearchLocator> {
            override fun createFromParcel(parcel: Parcel): SearchLocator {
                return SearchLocator(parcel)
            }

            override fun newArray(size: Int): Array<SearchLocator?> {
                return arrayOfNulls(size)
            }
        }
    }
}