/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

class Contributor : JSONable, Serializable {

    var multilanguageName: MultilanguageString = MultilanguageString()
    var sortAs: String? = null
    var roles: MutableList<String> = mutableListOf()
    var links: MutableList<Link> = mutableListOf()
    var identifier: String? = null

    var name: String? = null
        get() = multilanguageName.singleString

    override fun toJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("name", name)
        if (roles.isNotEmpty()) {
            obj.put("roles", getStringArray(roles))
        }
        obj.put("sortAs", sortAs)
        return obj
    }

}

fun parseContributors(contributors: Any): List<Contributor> {
    val result: MutableList<Contributor> = mutableListOf()
    when (contributors) {
        is String -> {
            val c = Contributor()
            c.multilanguageName.singleString = contributors
            result.add(c)
        }
        is Array<*> -> {
            for(i in 0 until contributors.size - 1) {
                val c = Contributor()
                c.multilanguageName.singleString = contributors[i] as String
                result.add(c)
            }
        }
        is JSONObject -> {
            val c = parseContributor(contributors)
            result.add(c)
        }
        is JSONArray -> for (i in 0..(contributors.length() - 1)) {
            val obj = contributors.getJSONObject(i)
            val c = parseContributor(obj)
            result.add(c)
        }
    }
    return result
}

fun parseContributor(cDict: JSONObject): Contributor {
    val c = Contributor()

    if (cDict.has("name")) {
        if (cDict.get("name") is String) {
            c.multilanguageName.singleString = cDict.getString("name")
        } else if (cDict.get("name") is JSONObject) {
            val array = cDict.getJSONObject("name")
            c.multilanguageName.multiString = array as MutableMap<String, String>
        }

    }
    if (cDict.has("identifier")) {
        c.identifier = cDict.getString("identifier")
    }
    if (cDict.has("sort_as")) {
        c.sortAs = cDict.getString("sort_as")
    }
    if (cDict.has("role")) {
        c.roles.add(cDict.getString("role"))
    }
    if (cDict.has("links")) {
        cDict.get("links")?.let {
        val links = it as? JSONArray
                ?: JSONArray()
        for (i in 0..(links.length() - 1)) {
            val linkDict = links.getJSONObject(i)
            val link = parseLink(linkDict)
            c.links.add(link)
        }
    }
    }
    return c
}
