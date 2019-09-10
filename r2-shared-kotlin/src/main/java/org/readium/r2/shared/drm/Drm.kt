/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.drm

import java.io.Serializable


class Drm(var brand: Brand) : Serializable {

    var scheme: Scheme

    var profile: String? = null
    var license: DrmLicense? = null

    enum class Brand(var v: String) : Serializable {
        Lcp("lcp")
    }

    enum class Scheme(var v: String) : Serializable {
        Lcp("http://readium.org/2014/01/lcp")
    }

    init {
        when (brand) {
            Brand.Lcp -> scheme = Scheme.Lcp
        }
    }

}