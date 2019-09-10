/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.server

import java.io.File

class Fonts {
    val fonts: MutableMap<String, File> = mutableMapOf()

    fun add(key: String, body: File) {
        fonts[key] = body
    }

    fun get(key: String): File {
        return fonts[key]!!
    }
}