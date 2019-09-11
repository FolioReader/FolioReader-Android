/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.server.handler


import android.webkit.MimeTypeMap
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import fi.iki.elonen.router.RouterNanoHTTPD
import org.readium.r2.streamer.server.Fonts
import java.io.InputStream


class FontHandler : RouterNanoHTTPD.DefaultHandler() {

    override fun getMimeType(): String? {
        return null
    }

    override fun getText(): String {
        return ResponseStatus.FAILURE_RESPONSE
    }

    override fun getStatus(): NanoHTTPD.Response.IStatus {
        return Status.OK
    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource?, urlParams: Map<String, String>?, session: NanoHTTPD.IHTTPSession?): Response {

        val method = session!!.method
        var uri = session.uri

        println("$TAG Method: $method, Url: $uri")

        return try {
            val lastSlashIndex = uri.lastIndexOf('/')
            uri = uri.substring(lastSlashIndex + 1, uri.length)
            val resources = uriResource!!.initParameter(Fonts::class.java)
            val x = createResponse(Status.OK, getMimeType(uri), resources.get(uri).inputStream())
            x
        } catch (e: Exception) {
            println(TAG + " Exception " + e.toString())
            newFixedLengthResponse(Status.INTERNAL_ERROR, mimeType, ResponseStatus.FAILURE_RESPONSE)
        }
    }

    private fun getMimeType(url: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        var mimeType = "application/vnd.ms-opentype"
        if (extension != null) {
            try {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            } catch (e: Exception) {
                when (extension) {
                    ".otf" -> mimeType = "application/vnd.ms-opentype"
                    ".ttf" -> mimeType = "application/vnd.ms-truetype"
                // TODO handle other font types
                }
            }
        }
        return mimeType
    }

    private fun createResponse(status: Status, mimeType: String, message: InputStream): Response {
        val response = NanoHTTPD.newChunkedResponse(status, mimeType, message)
        response.addHeader("Accept-Ranges", "bytes")
        return response
    }

    companion object {
        private const val TAG = "FontHandler"
    }
}
