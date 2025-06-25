package com.iteratehq.iterate.view

import android.os.Bundle
import com.iteratehq.iterate.model.StringToAnyMap
import com.iteratehq.iterate.model.Survey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.HashMap

@RunWith(RobolectricTestRunner::class)
class SurveyViewDeserializationTest {

    @Test
    fun `test EventTraits deserialization handles HashMap correctly`() {
        // Create a HashMap that simulates what Android returns after deserialization
        val hashMap = HashMap<String, Any>().apply {
            put("user_id", "12345")
            put("is_premium", true)
            put("account_age", 365)
        }
        
        // Create a bundle and put the HashMap as Serializable
        val bundle = Bundle().apply {
            putSerializable("event_traits", hashMap)
        }
        
        // Simulate the deserialization logic from setupView
        val eventTraitsObj = bundle.getSerializable("event_traits")
        val eventTraits = when (eventTraitsObj) {
            is StringToAnyMap -> eventTraitsObj
            is HashMap<*, *> -> {
                StringToAnyMap().apply {
                    eventTraitsObj.forEach { (key, value) ->
                        if (key is String && value != null) {
                            this[key] = value
                        }
                    }
                }
            }
            else -> null
        }
        
        // Verify the conversion worked correctly
        assertNotNull(eventTraits)
        assertTrue(eventTraits is StringToAnyMap)
        assertEquals("12345", eventTraits?.get("user_id"))
        assertEquals(true, eventTraits?.get("is_premium"))
        assertEquals(365, eventTraits?.get("account_age"))
    }
    
    @Test
    fun `test EventTraits deserialization handles StringToAnyMap correctly`() {
        // Create a StringToAnyMap
        val stringToAnyMap = StringToAnyMap(
            "user_id" to "12345",
            "is_premium" to true,
            "account_age" to 365
        )
        
        // Create a bundle and put the StringToAnyMap as Serializable
        val bundle = Bundle().apply {
            putSerializable("event_traits", stringToAnyMap)
        }
        
        // Simulate the deserialization logic from setupView
        val eventTraitsObj = bundle.getSerializable("event_traits")
        val eventTraits = when (eventTraitsObj) {
            is StringToAnyMap -> eventTraitsObj
            is HashMap<*, *> -> {
                StringToAnyMap().apply {
                    eventTraitsObj.forEach { (key, value) ->
                        if (key is String && value != null) {
                            this[key] = value
                        }
                    }
                }
            }
            else -> null
        }
        
        // Verify it's still the same object type
        assertNotNull(eventTraits)
        assertTrue(eventTraits is StringToAnyMap)
        assertEquals("12345", eventTraits?.get("user_id"))
        assertEquals(true, eventTraits?.get("is_premium"))
        assertEquals(365, eventTraits?.get("account_age"))
    }
    
    @Test
    fun `test EventTraits deserialization handles null correctly`() {
        val bundle = Bundle()
        
        val eventTraitsObj = bundle.getSerializable("event_traits")
        val eventTraits = when (eventTraitsObj) {
            is StringToAnyMap -> eventTraitsObj
            is HashMap<*, *> -> {
                StringToAnyMap().apply {
                    eventTraitsObj.forEach { (key, value) ->
                        if (key is String && value != null) {
                            this[key] = value
                        }
                    }
                }
            }
            else -> null
        }
        
        // Should be null when no data is present
        assertEquals(null, eventTraits)
    }
}