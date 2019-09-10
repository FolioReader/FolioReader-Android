/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import org.json.JSONObject
import org.readium.r2.shared.opds.IndirectAcquisition
import org.readium.r2.shared.opds.Price
import java.io.Serializable

class Properties : JSONable, Serializable {
    /// Suggested orientation for the device when displaying the linked resource.
    var orientation: String? = null
    /// Indicates how the linked resource should be displayed in a reading
    /// environment that displays synthetic spreads.
    var page: String? = null
    /// Identifies content contained in the linked resource, that cannot be
    /// strictly identified using a media type.
    var contains: MutableList<String> = mutableListOf()
    /// Location of a media-overlay for the resource referenced in the Link Object.
    private var mediaOverlay: String? = null
    /// Indicates that a resource is encrypted/obfuscated and provides relevant
    /// information for decryption.
    var encryption: Encryption? = null
    /// Hint about the nature of the layout for the linked resources.
    var layout: String? = null
    /// Suggested method for handling overflow while displaying the linked resource.
    var overflow: String? = null
    /// Indicates the condition to be met for the linked resource to be rendered
    /// within a synthetic spread.
    var spread: String? = null
    ///
    var numberOfItems: Int? = null
    ///
    var price: Price? = null
    ///
    var indirectAcquisition: MutableList<IndirectAcquisition> = mutableListOf()

    override fun toJSON(): JSONObject {
        val json = JSONObject()
        if (contains.isNotEmpty()) json.put("contains", getStringArray(contains))
        json.putOpt("mediaOverlay", mediaOverlay)
        json.putOpt("encryption", encryption)
        json.putOpt("layout", layout)
        json.putOpt("orientation", orientation)
        json.putOpt("overflow", overflow)
        json.putOpt("page", page)
        json.putOpt("spread", spread)
        return json
    }

}