package com.iteratehq.iterate.data.local

import com.iteratehq.iterate.model.EventTraits

internal interface IterateInMemoryStore {
    fun clear()
    fun getCompanyAuthToken(): String?
    fun getEventTraits(responseId: Long): EventTraits?
    fun getPreviewSurveyId(): String?
    fun setCompanyAuthToken(companyAuthToken: String)
    fun setEventTraits(eventTraits: EventTraits, responseId: Long)
    fun setPreviewSurveyId(previewSurveyId: String)
}

internal class DefaultIterateInMemoryStore : IterateInMemoryStore {
    private var companyAuthToken: String? = null
    private var eventTraits: Map<Long, EventTraits>? = null
    private var previewSurveyId: String? = null

    override fun clear() {
        companyAuthToken = null
        eventTraits = null
        previewSurveyId = null
    }

    override fun getCompanyAuthToken(): String? {
        return companyAuthToken
    }

    override fun getEventTraits(responseId: Long): EventTraits? {
        return eventTraits?.get(responseId)
    }

    override fun getPreviewSurveyId(): String? {
        return previewSurveyId
    }

    override fun setCompanyAuthToken(companyAuthToken: String) {
        this.companyAuthToken = companyAuthToken
    }

    override fun setEventTraits(eventTraits: EventTraits, responseId: Long) {
        this.eventTraits = mapOf(responseId to eventTraits)
    }

    override fun setPreviewSurveyId(previewSurveyId: String) {
        this.previewSurveyId = previewSurveyId
    }
}
