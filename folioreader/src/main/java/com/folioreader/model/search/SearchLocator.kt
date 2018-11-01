package com.folioreader.model.search

import android.os.Parcel
import android.os.Parcelable
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator
import org.readium.r2.shared.LocatorText

// TODO -> Move to locator package
class SearchLocator : Locator, Parcelable {

    var primaryContents: String? = text?.before + text?.hightlight + text?.after
    var searchItemType: SearchItemType

    constructor() : this(Locator("", 0, "", Locations(), null), SearchItemType.UNKNOWN_ITEM)

    constructor(locator: Locator, searchItemType: SearchItemType) :
            super(locator.href, locator.created, locator.title, locator.locations, locator.text) {
        this.searchItemType = searchItemType
    }

    constructor(parcel: Parcel) : this(
            Locator(
                    parcel.readString()!!,
                    parcel.readLong(),
                    parcel.readString()!!,
                    parcel.readSerializable() as Locations,
                    parcel.readSerializable() as LocatorText?
            ), SearchItemType.valueOf(parcel.readString()!!))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(href)
        parcel.writeLong(created)
        parcel.writeString(title)
        parcel.writeSerializable(locations)
        parcel.writeSerializable(text)
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