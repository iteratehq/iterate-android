package com.iteratehq.iterate.data.local

internal interface IterateInMemoryCache {
    fun getCompanyAuthToken(): String?
    fun getPreviewSurveyId(): String?
    fun setCompanyAuthToken(companyAuthToken: String)
    fun setPreviewSurveyId(previewSurveyId: String)
}

internal object IterateInMemoryCacheImpl : IterateInMemoryCache {
    private var companyAuthToken: String? = null
    private var previewSurveyId: String? = null

    override fun getCompanyAuthToken(): String? {
        return companyAuthToken
    }

    override fun getPreviewSurveyId(): String? {
        return previewSurveyId
    }

    override fun setCompanyAuthToken(companyAuthToken: String) {
        this.companyAuthToken = companyAuthToken
    }

    override fun setPreviewSurveyId(previewSurveyId: String) {
        this.previewSurveyId = previewSurveyId
    }
}
