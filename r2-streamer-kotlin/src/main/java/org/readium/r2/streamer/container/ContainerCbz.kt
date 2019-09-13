/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.container

import org.readium.r2.shared.RootFile
import org.readium.r2.shared.drm.Drm
import org.readium.r2.streamer.parser.mimetypeCBZ
import java.io.File
import java.util.zip.ZipFile


class ContainerCbz : CbzContainer, ZipArchiveContainer {

    override var rootFile: RootFile
    override var zipFile: ZipFile
    override var drm: Drm? = null
    override var successCreated: Boolean = false

    constructor(path: String) {

        if (File(path).exists()) {
            successCreated = true
        }
        zipFile = ZipFile(path)
        rootFile = RootFile(path, mimetypeCBZ)
    }

    override fun getFilesList(): List<String> {
        val filesList = mutableListOf<String>()
        zipFile.let {
            val listEntries = it.entries()
            listEntries.toList().forEach { filesList.add(it.toString()) }
        }
        return filesList
    }
}