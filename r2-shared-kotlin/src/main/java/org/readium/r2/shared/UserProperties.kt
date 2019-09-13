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

sealed class UserProperty(var ref: String, var name: String) {

    private var value: String = ""
        get() = this.toString()

    abstract override fun toString(): String
    fun getJson(): String {
        return """{name:"$name",value:"${this}"}"""
    }

}


// TODO add here your new Subclasses of UserPreference. It has to be an abstract class inheriting from UserSetting.

class Enumerable(var index: Int, private val values: List<String>, ref: String, name: String) : UserProperty(ref, name) {
    override fun toString() = values[index]
}

class Incremental(var value: Float,
                  val min: Float,
                  val max: Float,
                  private val step: Float,
                  private val suffix: String,
                  ref: String,
                  name: String) : UserProperty(ref, name) {

    fun increment() {
        value += (if (value + step <= max) step else 0.0f)
    }

    fun decrement() {
        value -= (if (value - step >= min) step else 0.0f)
    }

    override fun toString() = value.toString() + suffix
}

class Switchable(onValue: String, offValue: String, var on: Boolean, ref: String, name: String) : UserProperty(ref, name) {

    private val values = mapOf(true to onValue, false to offValue)

    fun switch() {
        on = !on
    }

    override fun toString() = values[on]!!
}


class UserProperties : Serializable {

    val properties: MutableList<UserProperty> = mutableListOf()

    fun addIncremental(nValue: Float, min: Float, max: Float, step: Float, suffix: String, ref: String, name: String) {
        properties.add(Incremental(nValue, min, max, step, suffix, ref, name))
    }

    fun addSwitchable(onValue: String, offValue: String, on: Boolean, ref: String, name: String) {
        properties.add(Switchable(onValue, offValue, on, ref, name))
    }

    fun addEnumerable(index: Int, values: List<String>, ref: String, name: String) {
        properties.add(Enumerable(index, values, ref, name))
    }

    fun <T : UserProperty> getByRef(ref: String) = properties.firstOrNull {
        it.ref == ref
    }!! as T
}

