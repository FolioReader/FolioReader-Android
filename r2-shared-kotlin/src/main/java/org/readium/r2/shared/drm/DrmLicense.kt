/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.drm

import org.joda.time.DateTime
import java.io.Serializable
import java.net.URL
import java.util.*

interface DrmLicense : Serializable {
    fun decipher(data: ByteArray): ByteArray?
    fun areRightsValid()
    fun register()
    fun renewLicense (endDate: DateTime? = null, callback: (Any) -> Unit)
    fun returnLicense(callback: (Any) -> Unit)
    fun currentStatus(): String
    fun lastUpdate(): Date
    fun issued(): Date
    fun provider(): URL
    fun rightsEnd(): Date?
    fun potentialRightsEnd(): Date?
    fun rightsStart(): Date?
    fun rightsPrints(): Int?
    fun rightsCopies(): Int?
}