/*
 * Module: r2-streamer-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.streamer.parser

import org.readium.r2.shared.Publication
import org.readium.r2.streamer.container.Container

data class PubBox(var publication: Publication, var container: Container)

interface PublicationParser {

    fun parse(fileAtPath: String, title: String = fileAtPath): PubBox?

}

fun normalize(base: String, href: String?): String {
    if (href == null || href.isEmpty())
        return ""
    val hrefComponents = href.split('/').filter { !it.isEmpty() }
    var baseComponents = base.split('/').filter { !it.isEmpty() }

    // Remove the /folder/folder/"PATH.extension" part to keep only the path.
    baseComponents = baseComponents.dropLast(1)
    // Find the number of ".." in the path to replace them.
    val replacementsNumber = hrefComponents.filter { it == ".." }.count()
    // Get the valid part of href, reversed for next operation.
    var normalizedComponents = hrefComponents.filter { it != ".." }
    // Add the part from base to replace the "..".
    for (i in 0 until replacementsNumber) {
        baseComponents = baseComponents.dropLast(1)
    }
    normalizedComponents = baseComponents + normalizedComponents
    // Recreate a string.
    var normalizedString = ""
    for (component in normalizedComponents) {
        normalizedString += "/$component"
    }
    return normalizedString
}
