/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.parser.epub

import org.readium.r2.shared.Link
import org.readium.r2.shared.parser.xml.XmlParser
import org.readium.r2.shared.parser.xml.Node
import org.readium.r2.streamer.parser.normalize

class NCXParser {

    lateinit var ncxDocumentPath: String

    fun tableOfContents(document: XmlParser): List<Link> {
        val navMapElement = document.root().getFirst("navMap")
        return nodeArray(navMapElement, "navPoint")
    }

    fun pageList(document: XmlParser): List<Link> {
        val pageListElement = document.root().getFirst("pageList")
        return nodeArray(pageListElement, "pageTarget")
    }

    private fun nodeArray(element: Node?, type: String): List<Link> {
        // The "to be returned" node array.
        val newNodeArray: MutableList<Link> = mutableListOf()

        // Find the elements of `type` in the XML element.
        val elements = element?.get(type) ?: return emptyList()
        // For each element create a new node of type `type`.
        for (newNode in elements.map { node(it, type) })
            newNodeArray.plusAssign(newNode)
        return newNodeArray
    }

    private fun node(element: Node, type: String): Link {
        val newNode = Link()
        newNode.href = normalize(ncxDocumentPath, element.getFirst("content")?.attributes?.get("src"))
        newNode.title = element.getFirst("navLabel")!!.getFirst("text")!!.text
        element.get("navPoint")?.let {
            for (childNode in it) {
                newNode.children.plusAssign(node(childNode, type))
            }
        }
        return newNode
    }

}