/*
 * Module: r2-shared-kotlin
 * Developers: Aferdita Muriqi, Clément Baumann
 *
 * Copyright (c) 2018. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.task

fun Request.promise(): Promise<Triple<Request, Response, ByteArray>, Exception> {
    val deferred = deferred<Triple<Request, Response, ByteArray>, Exception>()
    task { response() } success {
        val (request, response, result) = it
        when (result) {
            is Result.Success -> deferred.resolve(Triple(request, response, result.value))
            is Result.Failure -> deferred.reject(result.error)
        }
    } fail {
        deferred.reject(it)
    }
    return deferred.promise
}

val Response.contentTypeEncoding: String
    get() = contentTypeEncoding()

fun Response.contentTypeEncoding(default: String = "utf-8"): String {
    val contentType: String = headers["Content-Type"]?.first() ?: return default
    return contentType.substringAfterLast("charset=", default).substringAfter(' ', default)
}
