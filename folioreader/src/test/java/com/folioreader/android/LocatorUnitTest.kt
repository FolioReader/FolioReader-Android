package com.folioreader.android

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import org.junit.Test
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator

class LocatorUnitTest {

    // Remove below line to check test the difference
    //@Test
    fun testJackson() {

        val locations = Locations()
        locations.cfi = "epubcfi(/4/4)"
        var locator = Locator("/OEBPS/ch03.xhtml", 1539934158390, "", locations, null)

        val objectMapper = ObjectMapper()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val jsonString = objectMapper.writeValueAsString(locator)
        println("jsonString = $jsonString")

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        locator = objectMapper.readValue(jsonString)
        println(locator)
    }

    @Test
    fun testGson() {

        val locations = Locations()
        locations.cfi = "epubcfi(/4/4)"
        var locator = Locator("/OEBPS/ch03.xhtml", 1539934158390, "", locations, null)

        val gson = Gson()
        val jsonString = gson.toJson(locator)
        println("jsonString = $jsonString")

        locator = gson.fromJson(jsonString, Locator::class.java)
        println(locator.href)
    }
}