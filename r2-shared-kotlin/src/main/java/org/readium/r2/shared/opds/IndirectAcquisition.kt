/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.opds

import org.json.JSONObject
import java.io.Serializable


data class IndirectAcquisition(var typeAcquisition: String) : Serializable {
    var child = mutableListOf<IndirectAcquisition>()

}

enum class IndirectAcquisitionError(v: String) {
    InvalidJSON("OPDS 2 manifest is not valid JSON"),
    MetadataNotFound("Metadata not found"),
    InvalidMetadata("Invalid metadata"),
    InvalidLink("Invalid link"),
    InvalidIndirectAcquisition("Invalid indirect acquisition"),
    MissingTitle("Missing title"),
    InvalidFacet("Invalid facet"),
    InvalidGroup("Invalid group"),
    InvalidPublication("Invalid publication"),
    InvalidContributor("Invalid contributor"),
    InvalidCollection("Invalid collection"),
    InvalidNavigation("Invalid navigation")
}

fun parseIndirectAcquisition(indirectAcquisitionDict: JSONObject): IndirectAcquisition {
    val indirectAcquisitionType = indirectAcquisitionDict["type"] as? String
            ?: throw Exception(IndirectAcquisitionError.InvalidIndirectAcquisition.name)
    val indirectAcquisition = IndirectAcquisition(typeAcquisition = indirectAcquisitionType)
    val childDict = indirectAcquisitionDict.getJSONObject("child")
    val child = parseIndirectAcquisition(indirectAcquisitionDict = childDict)
    indirectAcquisition.child.add(child)
    return indirectAcquisition
}
