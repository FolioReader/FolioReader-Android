/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.parser.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class XmlParser {

    private var nodes: MutableList<Node> = mutableListOf()

    fun getFirst(name: String) = try {
        nodes.first { it.name == name }
    } catch (e: Exception) {
        null
    }

    fun root() = nodes.firstOrNull() ?: throw Exception("No root in xml document")

    fun parseXml(stream: InputStream) {
        nodes = mutableListOf()
        val parser = Xml.newPullParser()

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)
        parser.nextTag()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val node = Node(parser.name)
                    for (i in 0 until parser.attributeCount)
                        node.attributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                    if (!(nodes.isEmpty()))
                        nodes.last().children.add(node)
                    nodes.add(node)
                }
                XmlPullParser.END_TAG -> {
                    if (nodes.size > 1)
                        nodes.remove(nodes.last())
                }
                XmlPullParser.TEXT -> {
                    nodes.last().text += parser.text
                }
            }
            parser.nextToken()
        }
        stream.close()
    }
}