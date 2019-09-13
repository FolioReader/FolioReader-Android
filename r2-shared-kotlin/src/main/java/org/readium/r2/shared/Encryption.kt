/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import org.readium.r2.shared.drm.Drm
import java.io.Serializable

//  Contains metadata parsed from Encryption.xml
class Encryption : Serializable {
    //  Identifies the algorithm used to encrypt the resource
    var algorithm: String? = null
    //  Compression method used on the resource
    var compression: String? = null
    //  Original length of the resource in bytes before compression / encryption
    var originalLength: Int? = null
    //  Identifies the encryption profile used to encrypt the resource
    var profile: String? = null
    //  Identifies the encryption scheme used to encrypt the resource
    var scheme: Drm.Scheme? = null
}
