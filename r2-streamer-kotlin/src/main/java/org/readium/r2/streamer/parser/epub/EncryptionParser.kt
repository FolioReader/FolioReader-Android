/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.parser.epub

import org.readium.r2.shared.Encryption
import org.readium.r2.shared.Publication
import org.readium.r2.shared.parser.xml.Node
import org.readium.r2.streamer.parser.normalize

class EncryptionParser {

    fun parseEncryptionProperties(encryptedDataElement: Node, encryption: Encryption) {
        val encryptionProperties = encryptedDataElement.getFirst("EncryptionProperties")?.get("EncryptionProperty")
                ?: return
        for (encryptionProperty in encryptionProperties) {
            parseCompressionElement(encryptionProperty, encryption)
        }
    }

    private fun parseCompressionElement(encryptionProperty: Node, encryption: Encryption) {
        val compressionElement = encryptionProperty.getFirst("Compression") ?: return
        val originalLength = compressionElement.attributes["OriginalLength"]
        encryption.originalLength = originalLength?.toInt()
        val method = compressionElement.attributes["Method"] ?: return
        encryption.compression = if (method == "8") "deflate" else "none"
    }

    fun add(encryption: Encryption, publication: Publication, encryptedDataElement: Node) {
        var resourceURI = encryptedDataElement.getFirst("CipherData")?.getFirst("CipherReference")?.let { it.attributes["URI"] }
                ?: return
        resourceURI = normalize("/", resourceURI)
        val link = publication.linkWithHref(resourceURI) ?: return
        link.properties.encryption = encryption
    }

}