/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import java.io.Serializable

class MetadataItem : Serializable {
    var property: String? = null
    var value: String? = null
    var children: MutableList<MetadataItem> = mutableListOf()
}