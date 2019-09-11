/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.opds

import org.readium.r2.shared.Link
import org.readium.r2.shared.Publication
import java.io.Serializable
import java.net.URL


data class Feed(val title: String, val type: Int, val href: URL) : Serializable {
    var metadata: OpdsMetadata = OpdsMetadata(title = title)
    var links: MutableList<Link> = mutableListOf()
    var facets: MutableList<Facet> = mutableListOf()
    var groups: MutableList<Group> = mutableListOf()
    var publications: MutableList<Publication> = mutableListOf()
    var navigation: MutableList<Link> = mutableListOf()
    var context: MutableList<String> = mutableListOf()

    internal fun getSearchLinkHref(): String? {
        val searchLink = links.firstOrNull { it.rel.contains("search") }
        return searchLink?.href
    }
}

data class ParseData(val feed: Feed?, val publication: Publication?, val type: Int) : Serializable
