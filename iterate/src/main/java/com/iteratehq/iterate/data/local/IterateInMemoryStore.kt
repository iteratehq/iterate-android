package com.iteratehq.iterate.data.local

import com.iteratehq.iterate.model.EventTraits

internal interface IterateInMemoryStore {
    fun clear()

    fun getCompanyAuthToken(): String?

    fun getDisplayedSurveyResponseId(): Long?

    fun getEventTraitsMap(): Map<Long, EventTraits>?

    fun getPreviewSurveyId(): String?

    fun setCompanyAuthToken(companyAuthToken: String?)

    fun setDisplayedSurveyResponseId(responseId: Long?)

    fun setEventTraitsMap(eventTraitsMap: Map<Long, EventTraits>?)

    fun setPreviewSurveyId(previewSurveyId: String?)
}

internal class DefaultIterateInMemoryStore : IterateInMemoryStore {
    private var companyAuthToken: String? = null
    private var displayedSurveyResponseId: Long? = null
    private var eventTraitsMap: Map<Long, EventTraits>? = null
    private var previewSurveyId: String? = null

    override fun clear() {
        companyAuthToken = null
        displayedSurveyResponseId = null
        eventTraitsMap = null
        previewSurveyId = null
    }

    override fun getCompanyAuthToken(): String? {
        return companyAuthToken
    }

    override fun getDisplayedSurveyResponseId(): Long? {
        return displayedSurveyResponseId
    }

    override fun getEventTraitsMap(): Map<Long, EventTraits>? {
        return eventTraitsMap
    }

    override fun getPreviewSurveyId(): String? {
        return previewSurveyId
    }

    override fun setCompanyAuthToken(companyAuthToken: String?) {
        this.companyAuthToken = companyAuthToken
    }

    override fun setDisplayedSurveyResponseId(responseId: Long?) {
        this.displayedSurveyResponseId = responseId
    }

    override fun setEventTraitsMap(eventTraitsMap: Map<Long, EventTraits>?) {
        this.eventTraitsMap = eventTraitsMap
    }

    override fun setPreviewSurveyId(previewSurveyId: String?) {
        this.previewSurveyId = previewSurveyId
    }
}
