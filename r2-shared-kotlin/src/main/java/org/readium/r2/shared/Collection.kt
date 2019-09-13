/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import org.json.JSONObject

data class Collection(var name: String) {
    var sortAs: String? = null
    var identifier: String? = null
    var position: Double? = null
    var links: MutableList<Link> = mutableListOf()

}

enum class CollectionError(val v: String) {
    InvalidCollection("Invalid collection")
}

fun parseCollection(collectionDict: JSONObject): Collection {
    val name = collectionDict["name"] as? String
            ?: throw Exception(CollectionError.InvalidCollection.name)
    val c = Collection(name = name)

    if (collectionDict.has("sort_as")) {
        c.sortAs = collectionDict.getString("sort_as")
    }
    if (collectionDict.has("identifier")) {
        c.identifier = collectionDict.getString("identifier")
    }
    if (collectionDict.has("position")) {
        c.position = collectionDict.getDouble("position")
    }
    if (collectionDict.has("links")) {
        val links = collectionDict.getJSONArray("links")
                ?: throw Exception(CollectionError.InvalidCollection.name)
        for (i in 0..(links.length() - 1)) {
            val link = links.getJSONObject(i)
            c.links.add(parseLink(link))
        }
    }
    return c
}
