package com.iteratehq.iterate

import android.content.Context
import com.iteratehq.iterate.data.DefaultIterateRepository
import com.iteratehq.iterate.data.IterateRepository
import com.iteratehq.iterate.model.AppContext
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedType
import com.iteratehq.iterate.model.EventContext
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.Frequency
import com.iteratehq.iterate.model.Survey
import com.iteratehq.iterate.model.TargetingContext
import com.iteratehq.iterate.model.TrackingContext
import com.iteratehq.iterate.model.UserTraits

object Iterate {
    private lateinit var iterateRepository: IterateRepository

    /**
     * Minimal initialization that is expected to be called on app boot.
     *
     * @param context Activity or application context
     * @param apiKey Iterate API key
     */
    @JvmStatic
    fun init(context: Context, apiKey: String) {
        iterateRepository = DefaultIterateRepository(context.applicationContext)
        // TODO: initIdentify
        // TODO: initSendEvent
    }

    @JvmStatic
    fun identify(userTraits: UserTraits) {
        // TODO: Save userTraits into storage using repository
    }

    /**
     * Reset all stored user data. Commonly called on logout so apps can support multiple user
     * accounts.
     */
    @JvmStatic
    fun reset() {
        // TODO: clear storage and all caches, except the companyAuthToken
        // Only clear the storage if it has been initialized. This allows the reset
        // method to be called before Init, giving consumers of the SDK more flexibility.

        // TODO: set the company API key as the token
        // Reset the api client to the company API key
    }

    @JvmStatic
    fun preview(surveyId: String) {
        // TODO: store the surveyId inside IterateRepository (in-memory cache only)
    }

    @JvmStatic
    fun sendEvent(eventName: String, eventTraits: EventTraits?) {
        // TODO: get userTraits from IterateRepository
        // Embed context user traits
        val userTraits = null

        // TODO: get lastUpdated from IterateRepository
        // Embed context last updated
        val lastUpdated = null
        val tracking = TrackingContext(lastUpdated)

        // TODO: get previewSurveyId from IterateRepository
        // Embed context preview mode
        val previewSurveyId = null
        val targeting = if (previewSurveyId != null) {
            TargetingContext(Frequency.ALWAYS, previewSurveyId)
        } else {
            null
        }

        // Set the embed context
        val embedContext = EmbedContext(
            app = AppContext(
                version = "1.0.0", // TODO: get from BuildConfig
                urlScheme = null
            ),
            event = EventContext(eventName),
            type = EmbedType.MOBILE,
            targeting = targeting,
            tracking = tracking,
            userTraits = userTraits
        )

        // TODO: call embed API
    }

    @JvmSynthetic
    internal fun dispatchShowSurveyOrPrompt(survey: Survey, responseId: Int) {
        // TODO: show survey or prompt

        // TODO: call displayed API
    }
}
