package com.folioreader.android

import org.junit.Assert.assertEquals
import org.junit.Test
import org.readium.r2.shared.Locations
import org.readium.r2.shared.Locator

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun testNullability() {

        var locator = Locator("", 0, "", Locations(), null)

        //var string: String = locator.text?.before + locator.text?.hightlight + locator.text?.after

        var string: String = StringBuilder()
                .append(locator.text?.before)
                .append(locator.text?.hightlight ?: "")
                .append(locator.text?.after ?: "")
                .toString()

        println(string)
    }
}