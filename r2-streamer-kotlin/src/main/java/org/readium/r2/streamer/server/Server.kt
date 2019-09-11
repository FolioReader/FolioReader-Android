/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.server

import android.content.Context
import android.content.res.AssetManager
import fi.iki.elonen.router.RouterNanoHTTPD
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.container.Container
import org.readium.r2.streamer.fetcher.Fetcher
import org.readium.r2.streamer.server.handler.*
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

class Server(port: Int) : AbstractServer(port)

abstract class AbstractServer(private var port: Int) : RouterNanoHTTPD(port) {

    //    private val SEARCH_QUERY_HANDLE = "/search"
    private val MANIFEST_HANDLE = "/manifest"
    private val JSON_MANIFEST_HANDLE = "/manifest.json"
    private val MANIFEST_ITEM_HANDLE = "/(.*)"
    private val MEDIA_OVERLAY_HANDLE = "/media-overlay"
    private val CSS_HANDLE = "/styles/(.*)"
    private val JS_HANDLE = "/scripts/(.*)"
    private val FONT_HANDLE = "/fonts/(.*)"
    private var containsMediaOverlay = false

    private val resources = Ressources()
    private val fonts = Fonts()

    private fun addResource(name: String, body: String) {
        resources.add(name, body)
    }

    private fun addFont(name: String, assets: AssetManager, context: Context) {
        val inputStream = assets.open("fonts/$name")
        val dir = File(context.getExternalFilesDir(null).path + "/fonts/")
        dir.mkdirs()
        inputStream.toFile(context.getExternalFilesDir(null).path + "/fonts/" + name)
        val file = File(context.getExternalFilesDir(null).path + "/fonts/" + name)
        fonts.add(name, file)
    }

    fun loadResources(assets: AssetManager, context: Context) {
        addResource("ltr-after.css", Scanner(assets.open("ReadiumCSS/ltr/ReadiumCSS-after.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("ltr-before.css", Scanner(assets.open("ReadiumCSS/ltr/ReadiumCSS-before.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("ltr-default.css", Scanner(assets.open("ReadiumCSS/ltr/ReadiumCSS-default.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("rtl-after.css", Scanner(assets.open("ReadiumCSS/rtl/ReadiumCSS-after.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("rtl-before.css", Scanner(assets.open("ReadiumCSS/rtl/ReadiumCSS-before.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("rtl-default.css", Scanner(assets.open("ReadiumCSS/rtl/ReadiumCSS-default.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkv-after.css", Scanner(assets.open("ReadiumCSS/cjk-vertical/ReadiumCSS-after.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkv-before.css", Scanner(assets.open("ReadiumCSS/cjk-vertical/ReadiumCSS-before.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkv-default.css", Scanner(assets.open("ReadiumCSS/cjk-vertical/ReadiumCSS-default.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkh-after.css", Scanner(assets.open("ReadiumCSS/cjk-horizontal/ReadiumCSS-after.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkh-before.css", Scanner(assets.open("ReadiumCSS/cjk-horizontal/ReadiumCSS-before.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("cjkh-default.css", Scanner(assets.open("ReadiumCSS/cjk-horizontal/ReadiumCSS-default.css"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("touchHandling.js", Scanner(assets.open("ReadiumCSS/touchHandling.js"), "utf-8")
                .useDelimiter("\\A").next())
        addResource("utils.js", Scanner(assets.open("ReadiumCSS/utils.js"), "utf-8")
                .useDelimiter("\\A").next())
        addFont("OpenDyslexic-Regular.otf", assets, context)
    }


    fun addEpub(publication: Publication, container: Container, fileName: String, userPropertiesPath: String?) {
        val fetcher = Fetcher(publication, container, userPropertiesPath)

        addLinks(publication, fileName)

        publication.addSelfLink(fileName, URL("$BASE_URL:$port"))

        if (containsMediaOverlay) {
            addRoute(fileName + MEDIA_OVERLAY_HANDLE, MediaOverlayHandler::class.java, fetcher)
        }
        addRoute(fileName + JSON_MANIFEST_HANDLE, ManifestHandler::class.java, fetcher)
        addRoute(fileName + MANIFEST_HANDLE, ManifestHandler::class.java, fetcher)
        addRoute(fileName + MANIFEST_ITEM_HANDLE, ResourceHandler::class.java, fetcher)
        addRoute(JS_HANDLE, JSHandler::class.java, resources)
        addRoute(CSS_HANDLE, CSSHandler::class.java, resources)
        addRoute(FONT_HANDLE, FontHandler::class.java, fonts)
    }

    private fun addLinks(publication: Publication, filePath: String) {
        containsMediaOverlay = false
        for (link in publication.otherLinks) {
            if (link.rel.contains("media-overlay")) {
                containsMediaOverlay = true
                link.href = link.href?.replace("port", "localhost:$listeningPort$filePath")
            }
        }
    }

    private fun InputStream.toFile(path: String) {
        use { input ->
            File(path).outputStream().use { input.copyTo(it) }
        }
    }

}

