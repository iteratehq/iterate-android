package com.iteratehq.iterate.data.local

internal interface IterateInMemoryCache {
    fun getPreviewSurveyId(): String?
    fun setPreviewSurveyId(previewSurveyId: String)
}

internal object IterateInMemoryCacheImpl : IterateInMemoryCache {
    private var previewSurveyId: String? = null

    override fun getPreviewSurveyId(): String? {
        return previewSurveyId
    }

    override fun setPreviewSurveyId(previewSurveyId: String) {
        this.previewSurveyId = previewSurveyId
    }
}
