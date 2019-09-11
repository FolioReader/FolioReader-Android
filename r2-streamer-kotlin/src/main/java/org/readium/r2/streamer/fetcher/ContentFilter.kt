/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.fetcher

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.readium.r2.shared.*
import org.readium.r2.streamer.container.Container
import java.io.File
import java.io.InputStream

interface ContentFilters {
    var fontDecoder: FontDecoder
    var drmDecoder: DrmDecoder

    fun apply(input: InputStream, publication: Publication, container: Container, path: String): InputStream {
        return input
    }

    fun apply(input: ByteArray, publication: Publication, container: Container, path: String): ByteArray {
        return input
    }
}

class ContentFiltersEpub(private val userPropertiesPath: String?) : ContentFilters {

    override var fontDecoder = FontDecoder()
    override var drmDecoder = DrmDecoder()

    override fun apply(input: InputStream, publication: Publication, container: Container, path: String): InputStream {
        publication.linkWithHref(path)?.let { resourceLink ->

            var decodedInputStream = drmDecoder.decoding(input, resourceLink, container.drm)
            decodedInputStream = fontDecoder.decoding(decodedInputStream, publication, path)
            if ((resourceLink.typeLink == "application/xhtml+xml" || resourceLink.typeLink == "text/html")) {
                decodedInputStream = if (publication.metadata.rendition.layout == RenditionLayout.Reflowable && resourceLink.properties.layout == null
                        || resourceLink.properties.layout == "reflowable") {
                    injectReflowableHtml(decodedInputStream, publication)
                } else {
                    injectFixedLayoutHtml(decodedInputStream)
                }
            }

            return decodedInputStream
        } ?: run {
            return input
        }

    }

    override fun apply(input: ByteArray, publication: Publication, container: Container, path: String): ByteArray {
        publication.linkWithHref(path)?.let { resourceLink ->
            val inputStream = input.inputStream()
            var decodedInputStream = drmDecoder.decoding(inputStream, resourceLink, container.drm)
            decodedInputStream = fontDecoder.decoding(decodedInputStream, publication, path)
            val baseUrl = publication.baseUrl()?.removeLastComponent()
            if ((resourceLink.typeLink == "application/xhtml+xml" || resourceLink.typeLink == "text/html")
                    && baseUrl != null) {
                decodedInputStream =
                        if (publication.metadata.rendition.layout == RenditionLayout.Reflowable && (resourceLink.properties.layout == null
                                        || resourceLink.properties.layout == "reflowable")) {
                            injectReflowableHtml(decodedInputStream, publication)
                        } else {
                            injectFixedLayoutHtml(decodedInputStream)
                        }
            }
            return decodedInputStream.readBytes()
        } ?: run {
            return input
        }
    }

    private fun injectReflowableHtml(stream: InputStream, publication: Publication): InputStream {
        val data = stream.readBytes()
        var resourceHtml = String(data)
        // Inject links to css and js files
        var beginHeadIndex = resourceHtml.indexOf("<head>", 0, false) + 6
        var endHeadIndex = resourceHtml.indexOf("</head>", 0, false)
        if (endHeadIndex == -1)
            return stream

        val cssStyle = publication.cssStyle

        val endIncludes = mutableListOf<String>()
        val beginIncludes = mutableListOf<String>()
        beginIncludes.add("<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, maximum-scale=1.0, user-scalable=0\" />")

        beginIncludes.add(getHtmlLink("/styles/$cssStyle-before.css"))
        beginIncludes.add(getHtmlLink("/styles/$cssStyle-default.css"))
//        beginIncludes.add(getHtmlLink("/styles/transition.css"))
        endIncludes.add(getHtmlLink("/styles/$cssStyle-after.css"))
        endIncludes.add(getHtmlScript("/scripts/touchHandling.js"))
        endIncludes.add(getHtmlScript("/scripts/utils.js"))

        for (element in beginIncludes) {
            resourceHtml = StringBuilder(resourceHtml).insert(beginHeadIndex, element).toString()
            beginHeadIndex += element.length
            endHeadIndex += element.length
        }
        for (element in endIncludes) {
            resourceHtml = StringBuilder(resourceHtml).insert(endHeadIndex, element).toString()
            endHeadIndex += element.length
        }
        resourceHtml = StringBuilder(resourceHtml).insert(endHeadIndex, getHtmlFont("/fonts/OpenDyslexic-Regular.otf")).toString()
        resourceHtml = StringBuilder(resourceHtml).insert(endHeadIndex, "<style>@import url('https://fonts.googleapis.com/css?family=PT+Serif|Roboto|Source+Sans+Pro|Vollkorn');</style>\n").toString()

        // Inject userProperties
        getProperties(publication.userSettingsUIPreset)?.let { propertyPair ->
            val html = Regex("""<html.*>""").find(resourceHtml, 0)
            html?.let {
                val match = Regex("""(style=("([^"]*)"[ >]))|(style='([^']*)'[ >])""").find(html.value, 0)
                if (match != null) {
                    val beginStyle = match.range.start + 7
                    var newHtml = html.value
                    newHtml = StringBuilder(newHtml).insert(beginStyle, "${buildStringProperties(propertyPair)} ").toString()
                    resourceHtml = StringBuilder(resourceHtml).replace(Regex("""<html.*>"""), newHtml)
                } else {
                    val beginHtmlIndex = resourceHtml.indexOf("<html", 0, false) + 5
                    resourceHtml = StringBuilder(resourceHtml).insert(beginHtmlIndex, " style=\"${buildStringProperties(propertyPair)}\"").toString()
                }
            } ?:run {
                val beginHtmlIndex = resourceHtml.indexOf("<html", 0, false) + 5
                resourceHtml = StringBuilder(resourceHtml).insert(beginHtmlIndex, " style=\"${buildStringProperties(propertyPair)}\"").toString()
            }
        }
        return resourceHtml.toByteArray().inputStream()
    }

    private fun injectFixedLayoutHtml(stream: InputStream): InputStream {
        val data = stream.readBytes()
        var resourceHtml = String(data) //UTF-8
        val endHeadIndex = resourceHtml.indexOf("</head>", 0, false)
        if (endHeadIndex == -1)
            return stream
        val includes = mutableListOf<String>()
        includes.add(getHtmlScript("/scripts/touchHandling.js"))
        includes.add(getHtmlScript("/scripts/utils.js"))
        for (element in includes) {
            resourceHtml = StringBuilder(resourceHtml).insert(endHeadIndex, element).toString()
        }
        return resourceHtml.toByteArray().inputStream()
    }

    private fun getHtmlFont(resourceName: String): String {
        val prefix = "<style type=\"text/css\"> @font-face{font-family: \"OpenDyslexic\"; src:url(\""
        val suffix = "\") format('truetype');}</style>\n"
        return prefix + resourceName + suffix
    }

    private fun getHtmlLink(resourceName: String): String {
        val prefix = "<link rel=\"stylesheet\" type=\"text/css\" href=\""
        val suffix = "\"/>\n"
        return prefix + resourceName + suffix
    }

    private fun getHtmlScript(resourceName: String): String {
        val prefix = "<script type=\"text/javascript\" src=\""
        val suffix = "\"></script>\n"

        return prefix + resourceName + suffix
    }

    private fun getProperties(preset: MutableMap<ReadiumCSSName, Boolean>): MutableMap<String, String>? {

        // userProperties is a JSON string containing the css userProperties
        var userPropertiesString: String? = null
        userPropertiesPath?.let {
            userPropertiesString = String()
            val file = File(it)
            if (file.isFile && file.canRead()) {
                for (i in file.readLines()) {
                    userPropertiesString += i
                }
            }
        }

        return userPropertiesString?.let {
            // Parsing of the String into a JSONArray of JSONObject with each "name" and "value" of the css properties
            // Making that JSONArray a MutableMap<String, String> to make easier the access of data
            return@let try {
                val propertiesArray = JSONArray(userPropertiesString)
                val properties: MutableMap<String, String> = mutableMapOf()
                for (i in 0..(propertiesArray.length() - 1)) {
                    val value = JSONObject(propertiesArray.getString(i))
                    var isInPreset = false

                    for (property in preset) {
                        if (property.key.ref == value.getString("name")) {
                            isInPreset = true
                            val presetPair = Pair(property.key, preset[property.key])
                            val presetValue = applyPreset(presetPair)
                            properties.put(presetValue.getString("name"), presetValue.getString("value"))
                        }
                    }

                    if (!isInPreset) {
                        properties.put(value.getString("name"), value.getString("value"))
                    }

                }
                properties
            } catch (e: Exception) {
                Log.e("ContentFilter", "Error parsing json : $e")
                null
            }
        }
    }


    private fun applyPreset(preset: Pair<ReadiumCSSName, Boolean?>): JSONObject {
        val readiumCSSProperty = JSONObject()

        readiumCSSProperty.put("name", preset.first.ref)

        when(preset.first) {
            ReadiumCSSName.hyphens -> {
                readiumCSSProperty.put("value", "")
            }
            ReadiumCSSName.fontOverride -> {
                readiumCSSProperty.put("value", "readium-font-off")
            }
            ReadiumCSSName.appearance -> {
                readiumCSSProperty.put("value", "readium-default-on")
            }
            ReadiumCSSName.publisherDefault -> {
                readiumCSSProperty.put("value", "")
            }
            ReadiumCSSName.columnCount -> {
                readiumCSSProperty.put("value", "auto")
            }
            ReadiumCSSName.pageMargins -> {
                readiumCSSProperty.put("value", "0.5")
            }
            ReadiumCSSName.lineHeight -> {
                readiumCSSProperty.put("value", "1.0")
            }
            ReadiumCSSName.ligatures -> {
                readiumCSSProperty.put("value", "")
            }
            ReadiumCSSName.fontFamily -> {
                readiumCSSProperty.put("value", "Original")
            }
            ReadiumCSSName.fontSize -> {
                readiumCSSProperty.put("value", "100%")
            }
            ReadiumCSSName.wordSpacing -> {
                readiumCSSProperty.put("value", "0.0rem")
            }
            ReadiumCSSName.letterSpacing -> {
                readiumCSSProperty.put("value", "0.0em")
            }
            ReadiumCSSName.textAlignment -> {
                readiumCSSProperty.put("value", "justify")
            }
            ReadiumCSSName.paraIndent -> {
                readiumCSSProperty.put("value", "")
            }
            ReadiumCSSName.scroll -> {
                if (preset.second!!) {
                    readiumCSSProperty.put("value", "readium-scroll-on")
                } else {
                    readiumCSSProperty.put("value", "readium-scroll-off")
                }
            }
        }

        return readiumCSSProperty
    }

    private fun buildStringProperties(list: MutableMap<String, String>): String {
        var string = ""
        for (property in list) {
            string = string + " " + property.key + ": " + property.value + ";"
        }
        return string
    }


}

val ltrPreset: MutableMap<ReadiumCSSName, Boolean> = mutableMapOf(
        ReadiumCSSName.ref("hyphens") to false,
        ReadiumCSSName.ref("ligatures") to false
)

val rtlPreset: MutableMap<ReadiumCSSName, Boolean> = mutableMapOf(
        ReadiumCSSName.ref("hyphens") to false,
        ReadiumCSSName.ref("wordSpacing") to false,
        ReadiumCSSName.ref("letterSpacing") to false,
        ReadiumCSSName.ref("ligatures") to true
)

val cjkHorizontalPreset: MutableMap<ReadiumCSSName, Boolean> = mutableMapOf(
        ReadiumCSSName.ref("textAlignment") to false,
        ReadiumCSSName.ref("hyphens") to false,
        ReadiumCSSName.ref("paraIndent") to false,
        ReadiumCSSName.ref("wordSpacing") to false,
        ReadiumCSSName.ref("letterSpacing") to false
)

val cjkVerticalPreset: MutableMap<ReadiumCSSName, Boolean> = mutableMapOf(
        ReadiumCSSName.ref("scroll") to true,
        ReadiumCSSName.ref("columnCount") to false,
        ReadiumCSSName.ref("textAlignment") to false,
        ReadiumCSSName.ref("hyphens") to false,
        ReadiumCSSName.ref("paraIndent") to false,
        ReadiumCSSName.ref("wordSpacing") to false,
        ReadiumCSSName.ref("letterSpacing") to false
)

val forceScrollPreset: MutableMap<ReadiumCSSName, Boolean> = mutableMapOf(
        ReadiumCSSName.ref("scroll") to true
)

val userSettingsUIPreset: MutableMap<ContentLayoutStyle, MutableMap<ReadiumCSSName, Boolean>> = mutableMapOf(
        ContentLayoutStyle.layout("ltr") to ltrPreset,
        ContentLayoutStyle.layout("rtl") to rtlPreset,
        ContentLayoutStyle.layout("cjkv") to cjkVerticalPreset,
        ContentLayoutStyle.layout("cjkh") to cjkHorizontalPreset
)


class ContentFiltersCbz : ContentFilters {
    override var fontDecoder: FontDecoder = FontDecoder()
    override var drmDecoder: DrmDecoder = DrmDecoder()
}

