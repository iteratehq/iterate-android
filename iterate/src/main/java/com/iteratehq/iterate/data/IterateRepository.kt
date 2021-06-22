package com.iteratehq.iterate.data

import com.iteratehq.iterate.data.local.IterateInMemoryCache
import com.iteratehq.iterate.data.local.IterateSharedPrefs
import com.iteratehq.iterate.model.UserTraits

internal interface IterateRepository {
    fun getCompanyAuthToken(): String?
    fun getLastUpdated(): Long?
    fun getPreviewSurveyId(): String?
    fun getUserAuthToken(): String?
    fun getUserTraits(): UserTraits?
    fun setCompanyAuthToken(companyAuthToken: String)
    fun setLastUpdated(lastUpdated: Long)
    fun setPreviewSurveyId(previewSurveyId: String)
    fun setUserAuthToken(authToken: String)
    fun setUserTraits(userTraits: UserTraits)
}

internal class IterateRepositoryImpl(
    private val iterateInMemoryCache: IterateInMemoryCache,
    private val iterateSharedPrefs: IterateSharedPrefs,
) : IterateRepository {

    override fun getCompanyAuthToken(): String? {
        return iterateInMemoryCache.getCompanyAuthToken()
    }

    override fun getLastUpdated(): Long? {
        return iterateSharedPrefs.getLastUpdated()
    }

    override fun getPreviewSurveyId(): String? {
        return iterateInMemoryCache.getPreviewSurveyId()
    }

    override fun getUserAuthToken(): String? {
        return iterateSharedPrefs.getUserAuthToken()
    }

    override fun getUserTraits(): UserTraits? {
        return iterateSharedPrefs.getUserTraits()
    }

    override fun setCompanyAuthToken(companyAuthToken: String) {
        iterateInMemoryCache.setCompanyAuthToken(companyAuthToken)
    }

    override fun setLastUpdated(lastUpdated: Long) {
        iterateSharedPrefs.setLastUpdated(lastUpdated)
    }

    override fun setPreviewSurveyId(previewSurveyId: String) {
        iterateInMemoryCache.setPreviewSurveyId(previewSurveyId)
    }

    override fun setUserAuthToken(userAuthToken: String) {
        iterateSharedPrefs.setUserAuthToken(userAuthToken)
    }

    override fun setUserTraits(userTraits: UserTraits) {
        iterateSharedPrefs.setUserTraits(userTraits)
    }
}
