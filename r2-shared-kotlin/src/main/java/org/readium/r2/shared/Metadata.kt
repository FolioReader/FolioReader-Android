/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import org.readium.r2.shared.metadata.BelongsTo
import java.io.Serializable
import java.util.*

class Metadata : Serializable {
    /// The structure used for the serialisation.
    var multilanguageTitle: MultilanguageString? = null
    /// The title of the publication.
    var title: String = ""
        get() = multilanguageTitle?.singleString ?: ""

    var languages: MutableList<String> = mutableListOf()
    lateinit var identifier: String
    // Contributors.
    var authors: MutableList<Contributor> = mutableListOf()
    var translators: MutableList<Contributor> = mutableListOf()
    var editors: MutableList<Contributor> = mutableListOf()
    var artists: MutableList<Contributor> = mutableListOf()
    var illustrators: MutableList<Contributor> = mutableListOf()
    var letterers: MutableList<Contributor> = mutableListOf()
    var pencilers: MutableList<Contributor> = mutableListOf()
    var colorists: MutableList<Contributor> = mutableListOf()
    var inkers: MutableList<Contributor> = mutableListOf()
    var narrators: MutableList<Contributor> = mutableListOf()
    var imprints: MutableList<Contributor> = mutableListOf()
    var direction: String = PageProgressionDirection.default.name
    var subjects: MutableList<Subject> = mutableListOf()
    var publishers: MutableList<Contributor> = mutableListOf()
    var contributors: MutableList<Contributor> = mutableListOf()
    var modified: Date? = null
    var publicationDate: String? = null
    var description: String? = null
    var rendition: Rendition = Rendition()
    var source: String? = null
    var epubType: MutableList<String> = mutableListOf()
    var rights: String? = null
    var rdfType: String? = null
    var otherMetadata: MutableList<MetadataItem> = mutableListOf()

    var belongsTo: BelongsTo? = null

    var duration: Int? = null

    fun titleForLang(key: String): String? = multilanguageTitle?.multiString?.get(key)

    fun writeJSON(): JSONObject {
        val obj = JSONObject()
        obj.putOpt("languages", getStringArray(languages))
        obj.putOpt("publicationDate", publicationDate)
        obj.putOpt("identifier", identifier)
        obj.putOpt("modified", modified)
        obj.putOpt("title", title)
        obj.putOpt("rendition", rendition.getJSON())
        obj.putOpt("source", source)
        obj.putOpt("rights", rights)
        tryPut(obj, subjects, "subjects")
        tryPut(obj, authors, "authors")
        tryPut(obj, translators, "translators")
        tryPut(obj, editors, "editors")
        tryPut(obj, artists, "artists")
        tryPut(obj, illustrators, "illustrators")
        tryPut(obj, letterers, "letterers")
        tryPut(obj, pencilers, "pencilers")
        tryPut(obj, colorists, "colorists")
        tryPut(obj, inkers, "inkers")
        tryPut(obj, narrators, "narrators")
        tryPut(obj, contributors, "contributors")
        tryPut(obj, publishers, "publishers")
        tryPut(obj, imprints, "imprints")
        return obj
    }


    fun contentLayoutStyle(langType: LangType, pageDirection: String?) : ContentLayoutStyle {

        when(langType) {
            LangType.afh -> return ContentLayoutStyle.rtl
            LangType.cjk -> {
                if (pageDirection == ContentLayoutStyle.rtl.name)
                    return ContentLayoutStyle.cjkv
                else
                    return ContentLayoutStyle.cjkh
            }
            else -> {
                if (pageDirection == ContentLayoutStyle.rtl.name)
                    return ContentLayoutStyle.rtl
                else
                    return ContentLayoutStyle.ltr
            }
        }
    }

}

fun parseMetadata(metadataDict: JSONObject): Metadata {
    val m = Metadata()
    if (metadataDict.has("title")) {
        m.multilanguageTitle = MultilanguageString()
        m.multilanguageTitle?.singleString = metadataDict.getString("title")
    }
    if (metadataDict.has("identifier")) {
        m.identifier = metadataDict.getString("identifier")
    }
    if (metadataDict.has("@type")) {
        m.rdfType = metadataDict.getString("@type")
    } else if (metadataDict.has("type")) {
        m.rdfType = metadataDict.getString("type")
    }
    if (metadataDict.has("modified")) {
        m.modified = DateTime(metadataDict.getString("modified")).toDate()
    }
    if (metadataDict.has("author")) {
        m.authors.addAll(parseContributors(metadataDict.get("author")))
    }
    if (metadataDict.has("translator")) {
        m.translators.addAll(parseContributors(metadataDict.get("translator")))
    }
    if (metadataDict.has("editor")) {
        m.editors.addAll(parseContributors(metadataDict.get("editor")))
    }
    if (metadataDict.has("artist")) {
        m.artists.addAll(parseContributors(metadataDict.get("artist")))
    }
    if (metadataDict.has("illustrator")) {
        m.illustrators.addAll(parseContributors(metadataDict.get("illustrator")))
    }
    if (metadataDict.has("letterer")) {
        m.letterers.addAll(parseContributors(metadataDict.get("letterer")))
    }
    if (metadataDict.has("penciler")) {
        m.pencilers.addAll(parseContributors(metadataDict.get("penciler")))
    }
    if (metadataDict.has("colorist")) {
        m.colorists.addAll(parseContributors(metadataDict.get("colorist")))
    }
    if (metadataDict.has("inker")) {
        m.inkers.addAll(parseContributors(metadataDict.get("inker")))
    }
    if (metadataDict.has("narrator")) {
        m.narrators.addAll(parseContributors(metadataDict.get("narrator")))
    }
    if (metadataDict.has("contributor")) {
        m.contributors.addAll(parseContributors(metadataDict.get("contributor")))
    }
    if (metadataDict.has("publisher")) {
        m.publishers.addAll(parseContributors(metadataDict.get("publisher")))
    }
    if (metadataDict.has("imprint")) {
        m.imprints.addAll(parseContributors(metadataDict.get("imprint")))
    }
    if (metadataDict.has("published")) {
        m.publicationDate = metadataDict.getString("published")
    }
    if (metadataDict.has("description")) {
        m.description = metadataDict.getString("description")
    }
    if (metadataDict.has("source")) {
        m.source = metadataDict.getString("source")
    }
    if (metadataDict.has("rights")) {
        m.rights = metadataDict.getString("rights")
    }
    if (metadataDict.has("subject")) {
        val subjectDictUntyped = metadataDict.get("subject")

        when(subjectDictUntyped) {
            is String -> {
                val subject = Subject()
                subject.name = subjectDictUntyped
                m.subjects.add(subject)
            }
            is Array<*> -> {
                for(i in 0 until subjectDictUntyped.size - 1) {
                    val subject = Subject()
                    subject.name = subjectDictUntyped[i] as String
                    m.subjects.add(subject)
                }
            }
            is JSONArray -> {
                val subjDict = metadataDict.getJSONArray("subject")
                for (i in 0..(subjDict.length() - 1)) {
                    val sub = subjDict.getJSONObject(i)
                    val subject = Subject()
                    if (sub.has("name")) {
                        subject.name = sub.getString("name")
                    }
                    if (sub.has("sort_as")) {
                        subject.sortAs = sub.getString("sort_as")
                    }
                    if (sub.has("scheme")) {
                        subject.scheme = sub.getString("scheme")
                    }
                    if (sub.has("code")) {
                        subject.code = sub.getString("code")
                    }
                    m.subjects.add(subject)
                }
            }
        }


    }
    if (metadataDict.has("belongs_to")) {
        val belongsDict = metadataDict.getJSONObject("belongs_to")
        val belongs = BelongsTo()
        if (belongsDict.has("series")) {

            if (belongsDict.get("series") is JSONObject) {
                m.belongsTo?.series?.add(Collection(belongsDict.getString("series")))
            } else if (belongsDict.get("series") is JSONArray) {
                val array = belongsDict.getJSONArray("series")
                for (i in 0..(array.length() - 1)) {
                    val string = array.getString(i)
                    m.belongsTo?.series?.add(Collection(string))
                }
            }
        }

        if (belongsDict.has("collection")) {
            when {
                belongsDict.get("collection") is String -> m.belongsTo?.collection?.add(Collection(belongsDict.getString("collection")))
                belongsDict.get("collection") is JSONObject -> belongs.series.add(parseCollection(belongsDict.getJSONObject("collection")))
                belongsDict.get("collection") is JSONArray -> {
                    val array = belongsDict.getJSONArray("collection")
                    for (i in 0..(array.length() - 1)) {
                        val obj = array.getJSONObject(i)
                        belongs.series.add(parseCollection(obj))
                    }
                }
            }
        }
        m.belongsTo = belongs
    }

    if (metadataDict.has("duration")) {
        m.duration = metadataDict.getInt("duration")
    }
    if (metadataDict.has("language")) {
        if (metadataDict.get("language") is JSONObject) {
            m.languages.add(metadataDict.getString("language"))
        } else if (metadataDict.get("language") is JSONArray) {
            val array = metadataDict.getJSONArray("language")
            for (i in 0..(array.length() - 1)) {
                val string = array.getString(i)
                m.languages.add(string)
            }
        }
    }

    return m
}

enum class LangType {
    cjk, afh, other
}


enum class PageProgressionDirection {
    default,
    ltr,
    rtl
}

enum class ContentLayoutStyle {
    ltr,
    rtl,
    cjkv,
    cjkh;

    companion object {
        fun layout(name: String): ContentLayoutStyle = ContentLayoutStyle.valueOf(name)
    }
}