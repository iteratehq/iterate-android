package com.iteratehq.iterate.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StringToAnyMapTest {
    @Test
    fun `should be equal to a Map of String to Any`() {
        val stringToAnyMapObject = StringToAnyMap(
            "key1" to "value1",
            "key2" to 2,
            "key3" to true,
        )
        val mapObject = mapOf<String, Any>(
            "key1" to "value1",
            "key2" to 2,
            "key3" to true,
        )
        assertEquals(mapObject, stringToAnyMapObject)
    }
}
