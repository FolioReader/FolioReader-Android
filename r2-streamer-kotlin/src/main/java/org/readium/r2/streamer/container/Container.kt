/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.container

import org.readium.r2.shared.Link
import org.readium.r2.shared.RootFile
import org.readium.r2.shared.drm.Drm
import org.readium.r2.shared.parser.xml.XmlParser
import java.io.InputStream

/**
 * Container of a publication
 *
 * @var rootfile : a RootFile class containing the path the publication, the version
 *                 and the mime type of it
 *
 * @var drm : contain the brand, scheme, profile and license of DRM if it exist
 *
 * @var successCreated : used to checked if the Container contains a publication
 *
 * @func data : return the ByteArray content of a file from the publication
 *
 * @func dataLength : return the length of content
 *
 * @func dataInputStream : return the InputStream of content
 */
interface Container {

    var rootFile: RootFile

    var drm: Drm?

    var successCreated: Boolean

    fun data(relativePath: String): ByteArray

    fun dataLength(relativePath: String): Long

    fun dataInputStream(relativePath: String): InputStream
}

/**
 *  EpubContainer
 *
 *  @func xmlDocumentForFile : return the XmlParser of a file
 *
 *  @func xmlDocumentForResource : return the XmlParser of a link
 */
interface EpubContainer : Container {

    fun xmlDocumentForFile(relativePath: String): XmlParser
    fun xmlDocumentForResource(link: Link?): XmlParser
    fun xmlAsByteArray(link: Link?): ByteArray
    fun scanForDrm(): Drm?
}

/**
 * CbzContainer
 *
 * @func getFilesList : return the list of every files in a CBZ
 */
interface CbzContainer : Container {
    fun getFilesList(): List<String>
}
