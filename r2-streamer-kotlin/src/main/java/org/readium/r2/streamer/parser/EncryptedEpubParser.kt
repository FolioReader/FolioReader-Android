/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.parser

import org.readium.r2.streamer.container.ContainerEpubDirectory
import org.readium.r2.streamer.container.EncryptedContainerEpub
import org.readium.r2.streamer.container.EpubContainer
import timber.log.Timber
import java.io.File

class EncryptedEpubParser(private val key: String) : EpubParser() {

    override fun generateContainerFrom(path: String): EpubContainer {
        val isDirectory = File(path).isDirectory
        if (!File(path).exists()) {
            throw Exception("Missing File")
        }

        val container = when (isDirectory) {
            true -> ContainerEpubDirectory(path)
            false -> EncryptedContainerEpub(path, key)
        }

        if (!container.successCreated)
            throw Exception("Missing File")

        return container
    }
}
