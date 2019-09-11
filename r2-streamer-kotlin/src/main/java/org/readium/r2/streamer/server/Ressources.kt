/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.server

class Ressources {
    val resources: MutableMap<String, String> = mutableMapOf()

    fun add(key: String, body: String) {
        resources[key] = body
    }

    fun get(key: String): String {
        return resources[key] ?: ""
    }
}