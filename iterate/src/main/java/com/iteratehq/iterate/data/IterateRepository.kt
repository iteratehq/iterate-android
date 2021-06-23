package com.iteratehq.iterate.data

import android.content.Context
import com.iteratehq.iterate.data.local.DefaultIterateInMemoryStore
import com.iteratehq.iterate.data.local.DefaultIterateSharedPrefs
import com.iteratehq.iterate.data.local.IterateInMemoryStore
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
    fun setUserAuthToken(userAuthToken: String)
    fun setUserTraits(userTraits: UserTraits)
}

internal class DefaultIterateRepository @JvmOverloads internal constructor(
    context: Context,
    private val iterateInMemoryStore: IterateInMemoryStore = DefaultIterateInMemoryStore(),
    private val iterateSharedPrefs: IterateSharedPrefs = DefaultIterateSharedPrefs(context.applicationContext),
) : IterateRepository {

    override fun getCompanyAuthToken(): String? {
        return iterateInMemoryStore.getCompanyAuthToken()
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
