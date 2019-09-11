/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

class RootFile() {

    constructor(rootPath: String = "", rootFilePath: String = "", mimetype: String = "", version: Double? = null) : this() {
        this.rootPath = rootPath
        this.rootFilePath = rootFilePath
        this.mimetype = mimetype
        this.version = version
    }

    var rootPath: String = ""
    //  Path to OPF
    var rootFilePath: String = ""
    var mimetype: String = ""
    var version: Double? = null

}