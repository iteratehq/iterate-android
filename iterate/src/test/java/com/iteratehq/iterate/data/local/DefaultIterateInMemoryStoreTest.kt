package com.iteratehq.iterate.data.local

import com.iteratehq.iterate.model.EventTraits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultIterateInMemoryStoreTest {
    private val companyAuthToken = "companyAuthToken"
    private val displayedSurveyResponseId = 100L
    private val eventTraits = EventTraits("key" to "value")
    private val eventTraitsMap = mapOf(100L to eventTraits)
    private val previewSurveyId = "previewSurveyId"

    @Test
    fun `should correctly store companyAuthToken`() {
        val store = DefaultIterateInMemoryStore()
        store.setCompanyAuthToken(companyAuthToken)
        val outputCompanyAuthToken = store.getCompanyAuthToken()

        assertEquals(companyAuthToken, outputCompanyAuthToken)
    }

    @Test
    fun `should correctly store displayedSurveyResponseId`() {
        val store = DefaultIterateInMemoryStore()
        store.setDisplayedSurveyResponseId(displayedSurveyResponseId)
        val outputDisplayedSurveyResponseId = store.getDisplayedSurveyResponseId()

        assertEquals(displayedSurveyResponseId, outputDisplayedSurveyResponseId)
    }

    @Test
    fun `should correctly store eventTraitsMap`() {
        val store = DefaultIterateInMemoryStore()
        store.setEventTraitsMap(eventTraitsMap)
        val outputEventTraitsMap = store.getEventTraitsMap()

        assertEquals(eventTraitsMap, outputEventTraitsMap)
    }

    @Test
    fun `should correctly store previewSurveyId`() {
        val store = DefaultIterateInMemoryStore()
        store.setPreviewSurveyId(previewSurveyId)
        val outputPreviewSurveyId = store.getPreviewSurveyId()

        assertEquals(previewSurveyId, outputPreviewSurveyId)
    }

    @Test
    fun `should have all data set to null after being cleared`() {
        val store = DefaultIterateInMemoryStore().apply {
            setCompanyAuthToken(companyAuthToken)
            setDisplayedSurveyResponseId(displayedSurveyResponseId)
            setEventTraitsMap(eventTraitsMap)
            setPreviewSurveyId(previewSurveyId)
        }
        store.clear()

        val outputCompanyAuthToken = store.getCompanyAuthToken()
        val outputDisplayedSurveyResponseId = store.getDisplayedSurveyResponseId()
        val outputEventTraitsMap = store.getEventTraitsMap()
        val outputPreviewSurveyId = store.getPreviewSurveyId()

        assertNull(outputCompanyAuthToken)
        assertNull(outputDisplayedSurveyResponseId)
        assertNull(outputEventTraitsMap)
        assertNull(outputPreviewSurveyId)
    }
}
