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
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class NavigationDocumentParser {

    var navigationDocumentPath: String = ""

    fun tableOfContent(xml: ByteArray) : MutableList<Link> {
        val tableOfContents = mutableListOf<Link>()
        val document = xml.inputStream()

        val xpath = "/xhtml:html/xhtml:body/xhtml:nav[@epub:type='toc']//xhtml:a" + "|/xhtml:html/xhtml:body/xhtml:nav[@epub:type='toc']//xhtml:span"
        val nodes = evaluateXpath(xpath, document)

        for (i in 0 until nodes.length) {
            nodes.item(i).attributes.getNamedItem("href")?.let {
                val link = Link()
                link.href = normalize(navigationDocumentPath, it.nodeValue)
                link.title = nodes.item(i).textContent
                tableOfContents.add(link)
            }
        }

        return tableOfContents
    }

    fun pageList(document: XmlParser) = nodeArray(document, "page-list")
    fun landmarks(document: XmlParser) = nodeArray(document, "landmarks")
    fun listOfIllustrations(document: XmlParser) = nodeArray(document, "loi")
    fun listOfTables(document: XmlParser) = nodeArray(document, "lot")
    fun listOfAudiofiles(document: XmlParser) = nodeArray(document, "loa")
    fun listOfVideos(document: XmlParser) = nodeArray(document, "lov")

    private fun nodeArray(document: XmlParser, navType: String): List<Link> {
        var body = document.root().getFirst("body")
        body?.getFirst("section")?.let { body = it }
        val navPoint = body?.get("nav")?.firstOrNull { it.attributes["epub:type"] == navType }
        val olElement = navPoint?.getFirst("ol") ?: return emptyList()
        return nodeOl(olElement).children
    }

    private fun nodeOl(element: Node): Link {
        val newOlNode = Link()
        val liElements = element.get("li") ?: return newOlNode
        for (li in liElements) {
            val spanText = li.getFirst("span")?.name
            if (spanText != null && !spanText.isEmpty()) {
                li.getFirst("ol")?.let {
                    newOlNode.children.add(nodeOl(it))
                }
            } else {
                val childLiNode = nodeLi(li)
                newOlNode.children.add(childLiNode)
            }
        }
        return newOlNode
    }

    private fun nodeLi(element: Node): Link {
        val newLiNode = Link()
        val aNode = element.getFirst("a")!!
        val title = (aNode.getFirst("span"))?.text ?: aNode.text ?: aNode.name
        newLiNode.href = normalize(navigationDocumentPath, aNode.attributes["href"])
        newLiNode.title = title
        element.getFirst("ol")?.let { newLiNode.children.add(nodeOl(it)) }
        return newLiNode
    }

    private fun evaluateXpath(expression: String, doc: InputStream): NodeList {

        val dbFactory = DocumentBuilderFactory.newInstance()
        dbFactory.isNamespaceAware = true
        
        val docBuilder  = dbFactory.newDocumentBuilder()

        val document = docBuilder.parse(doc)

        val xPath = XPathFactory.newInstance().newXPath()
        xPath.namespaceContext = NameSpaceResolver()

        return xPath.evaluate(expression, document, XPathConstants.NODESET) as NodeList
    }
}

class NameSpaceResolver : NamespaceContext {
    override fun getNamespaceURI(p0: String?): String {
        when (p0) {
            null -> throw IllegalArgumentException("No prefix provided!")
            "epub" -> return "http://www.idpf.org/2007/ops"
            "xhtml" -> return "http://www.w3.org/1999/xhtml"
            else -> return XMLConstants.DEFAULT_NS_PREFIX
        }
    }

    override fun getPrefix(p0: String?): String? {
        return null
    }

    override fun getPrefixes(p0: String?): MutableIterator<Any?>? {
        return null
    }
}

