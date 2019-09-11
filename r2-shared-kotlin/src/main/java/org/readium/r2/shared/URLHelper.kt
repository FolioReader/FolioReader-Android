/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import java.net.URI

fun getAbsolute(href: String, base: String): String {
    val baseURI = URI.create(base)
    val relative = baseURI.resolve(href)
    return relative.toString()
}


internal fun normalize(base: String, in_href: String?) : String {
    val href = in_href
    if (href == null || href.isEmpty()) {
        return ""
    }
    val hrefComponents = href.split( "/").filter({ !it.isEmpty() })
    var baseComponents = base.split( "/").filter({ !it.isEmpty() })
    baseComponents.dropLast(1)

    val replacementsNumber = hrefComponents.filter({ it == ".." }).count()
    var normalizedComponents = hrefComponents.filter({ it != ".." })
    for (e in 0 until replacementsNumber) {
        baseComponents.dropLast(1)
    }
    normalizedComponents = baseComponents + normalizedComponents
    var normalizedString = ""
    for (component in normalizedComponents) {
        normalizedString += "/${component}"
    }
    return normalizedString
}



