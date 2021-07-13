package com.iteratehq.iterate

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.iteratehq.iterate.data.DefaultIterateRepository
import com.iteratehq.iterate.data.IterateRepository
import com.iteratehq.iterate.data.remote.ApiResponseCallback
import com.iteratehq.iterate.model.AppContext
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.EmbedType
import com.iteratehq.iterate.model.EventContext
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.Frequency
import com.iteratehq.iterate.model.Survey
import com.iteratehq.iterate.model.TargetingContext
import com.iteratehq.iterate.model.TrackingContext
import com.iteratehq.iterate.model.TriggerType
import com.iteratehq.iterate.model.UserTraits
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Iterate {
    private lateinit var iterateRepository: IterateRepository
    private lateinit var apiKey: String

    /**
     * Minimal initialization that is expected to be called on app boot.
     *
     * @param context Activity or application context
     * @param apiKey Iterate API key
     */
    @JvmStatic
    fun init(context: Context, apiKey: String) {
        this.iterateRepository = DefaultIterateRepository(context.applicationContext, apiKey)
        this.apiKey = apiKey
        initAuthToken()
    }

    /**
     * TODO: add explanation
     *
     * @throws IllegalStateException TODO: add explanation
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun identify(userTraits: UserTraits) {
        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.identify(). Make sure you call Iterate.init() before calling identify, see README for details")
        }
        iterateRepository.setUserTraits(userTraits)
    }

    /**
     * Reset all stored user data. Commonly called on logout so apps can support multiple user
     * accounts.
     */
    @JvmStatic
    fun reset() {
        // Only clear the storage if it has been initialized. This allows the reset
        // method to be called before Init, giving consumers of the SDK more flexibility
        if (::iterateRepository.isInitialized) {
            // Clear the storage and all caches, except for the companyAuthToken
            iterateRepository.clearExceptCompanyAuthToken()

            // Reset the api client to the company API key
            iterateRepository.setApiKey(this.apiKey)
        }
    }

    /**
     * TODO: add explanation
     *
     * @throws IllegalStateException TODO: add explanation
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun preview(surveyId: String) {
        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.preview(). Make sure you call Iterate.init() before calling preview, see README for details")
        }
        iterateRepository.setPreviewSurveyId(surveyId)
    }

    @JvmStatic
    @JvmOverloads
    fun sendEvent(
        eventName: String,
        eventTraits: EventTraits?,
        supportFragmentManager: FragmentManager? = null
    ) {
        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.sendEvent(). Make sure you call Iterate.init() before calling sendEvent, see README for details")
        }

        // Embed context user traits
        val userTraits = iterateRepository.getUserTraits()

        // Embed context last updated
        val lastUpdated = iterateRepository.getLastUpdated()
        val tracking = TrackingContext(lastUpdated)

        // Embed context preview mode
        val previewSurveyId = iterateRepository.getPreviewSurveyId()
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

        // Call embed API
        iterateRepository.embed(embedContext, object : ApiResponseCallback<EmbedResults> {
            override fun onSuccess(result: EmbedResults) {
                // Set the user auth token if one is returned
                result.auth?.token?.let { token ->
                    iterateRepository.setApiKey(token)
                }

                // Set the last updated time if one is returned
                result.tracking?.lastUpdated?.let { lastUpdated ->
                    iterateRepository.setLastUpdated(lastUpdated)
                }

                result.survey?.let { survey ->
                    if (supportFragmentManager != null) {
                        // Generate a unique id (current timestamp) for this survey display so we ensure
                        // we associate the correct event traits with it
                        val responseId = Date().time
                        if (eventTraits != null) {
                            iterateRepository.setEventTraits(eventTraits, responseId)
                        }

                        // If the survey has a timer trigger, wait that number of seconds before showing the survey
                        if (
                            !result.triggers.isNullOrEmpty() &&
                            result.triggers[0].type == TriggerType.SECONDS
                        ) {
                            CoroutineScope(Dispatchers.Default).launch {
                                val seconds = result.triggers[0].options.seconds ?: 0
                                delay(seconds * 1000L)
                                dispatchShowSurveyOrPrompt(
                                    survey,
                                    responseId,
                                    supportFragmentManager
                                )
                            }
                        } else {
                            dispatchShowSurveyOrPrompt(survey, responseId, supportFragmentManager)
                        }
                    }
                }
            }

            override fun onError(e: Exception) {
                Log.e("sendEvent", e.toString())
            }
        })
    }

    private fun initAuthToken() {
        val userAuthToken = iterateRepository.getUserAuthToken()
        if (userAuthToken != null) {
            iterateRepository.setApiKey(userAuthToken)
        }
    }

    private fun dispatchShowSurveyOrPrompt(
        survey: Survey,
        responseId: Long,
        supportFragmentManager: FragmentManager
    ) {
        if (survey.prompt != null) {
            // Show prompt
        } else {
            // Show survey
        }

        iterateRepository.displayed(survey)
    }
}
