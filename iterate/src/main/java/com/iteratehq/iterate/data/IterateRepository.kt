package com.iteratehq.iterate.data

import android.content.Context
import com.iteratehq.iterate.data.local.DefaultIterateInMemoryStore
import com.iteratehq.iterate.data.local.DefaultIterateSharedPrefs
import com.iteratehq.iterate.data.local.IterateInMemoryStore
import com.iteratehq.iterate.data.local.IterateSharedPrefs
import com.iteratehq.iterate.data.remote.ApiResponseCallback
import com.iteratehq.iterate.data.remote.DefaultIterateApi
import com.iteratehq.iterate.data.remote.IterateApi
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.Survey
import com.iteratehq.iterate.model.UserTraits

internal interface IterateRepository {
    fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>?
    )
    fun displayed(survey: Survey)
    fun dismissed(survey: Survey)
    fun setApiKey(apiKey: String)
    fun clearExceptCompanyAuthToken()
    fun getCompanyAuthToken(): String?
    fun getEventTraits(responseId: Long): EventTraits?
    fun getLastUpdated(): Long?
    fun getPreviewSurveyId(): String?
    fun getUserAuthToken(): String?
    fun getUserTraits(): UserTraits?
    fun setCompanyAuthToken(companyAuthToken: String)
    fun setEventTraits(eventTraits: EventTraits, responseId: Long)
    fun setLastUpdated(lastUpdated: Long)
    fun setPreviewSurveyId(previewSurveyId: String)
    fun setUserAuthToken(userAuthToken: String)
    fun setUserTraits(userTraits: UserTraits)
}

internal class DefaultIterateRepository @JvmOverloads internal constructor(
    context: Context,
    apiKey: String,
    useEncryptedSharedPreferences: Boolean = true,
    private var iterateApi: IterateApi = DefaultIterateApi(apiKey),
    private val iterateInMemoryStore: IterateInMemoryStore = DefaultIterateInMemoryStore(),
    private val iterateSharedPrefs: IterateSharedPrefs = DefaultIterateSharedPrefs(context.applicationContext, useEncryptedSharedPreferences),
) : IterateRepository {

    override fun embed(embedContext: EmbedContext, callback: ApiResponseCallback<EmbedResults>?) {
        iterateApi.embed(embedContext, callback)
    }

    override fun displayed(survey: Survey) {
        iterateApi.displayed(survey)
    }

    override fun dismissed(survey: Survey) {
        iterateApi.dismissed(survey)
        iterateInMemoryStore.setDisplayedSurveyResponseId(null)
        iterateInMemoryStore.setEventTraitsMap(null)
    }

    override fun setApiKey(apiKey: String) {
        iterateApi = DefaultIterateApi(apiKey)
    }

    override fun clearExceptCompanyAuthToken() {
        val companyAuthToken = iterateInMemoryStore.getCompanyAuthToken()
        iterateInMemoryStore.clear()
        iterateSharedPrefs.clear()
        companyAuthToken?.let { iterateInMemoryStore.setCompanyAuthToken(it) }
    }

    override fun getCompanyAuthToken(): String? {
        return iterateInMemoryStore.getCompanyAuthToken()
    }

    override fun getEventTraits(responseId: Long): EventTraits? {
        val eventTraitsMap = iterateInMemoryStore.getEventTraitsMap()
        return eventTraitsMap?.get(responseId)
    }

    override fun getLastUpdated(): Long? {
        return iterateSharedPrefs.getLastUpdated()
    }

    override fun getPreviewSurveyId(): String? {
        return iterateInMemoryStore.getPreviewSurveyId()
    }

    override fun getUserAuthToken(): String? {
        return iterateSharedPrefs.getUserAuthToken()
    }

    override fun getUserTraits(): UserTraits? {
        return iterateSharedPrefs.getUserTraits()
    }

    override fun setCompanyAuthToken(companyAuthToken: String) {
        iterateInMemoryStore.setCompanyAuthToken(companyAuthToken)
    }

    override fun setEventTraits(eventTraits: EventTraits, responseId: Long) {
        val eventTraitsMap = mapOf(responseId to eventTraits)
        iterateInMemoryStore.setEventTraitsMap(eventTraitsMap)
    }

    override fun setLastUpdated(lastUpdated: Long) {
        iterateSharedPrefs.setLastUpdated(lastUpdated)
    }

    override fun setPreviewSurveyId(previewSurveyId: String) {
        iterateInMemoryStore.setPreviewSurveyId(previewSurveyId)
    }

    override fun setUserAuthToken(userAuthToken: String) {
        iterateSharedPrefs.setUserAuthToken(userAuthToken)
    }

    override fun setUserTraits(userTraits: UserTraits) {
        iterateSharedPrefs.setUserTraits(userTraits)
    }
}
