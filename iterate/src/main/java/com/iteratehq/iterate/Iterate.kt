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
import com.iteratehq.iterate.model.InteractionEventData
import com.iteratehq.iterate.model.InteractionEventSource
import com.iteratehq.iterate.model.InteractionEventTypes
import com.iteratehq.iterate.model.ProgressEventMessageData
import com.iteratehq.iterate.model.Question
import com.iteratehq.iterate.model.Response
import com.iteratehq.iterate.model.Survey
import com.iteratehq.iterate.model.TargetingContext
import com.iteratehq.iterate.model.TrackingContext
import com.iteratehq.iterate.model.TriggerType
import com.iteratehq.iterate.model.UserTraits
import com.iteratehq.iterate.view.PromptView
import com.iteratehq.iterate.view.SurveyView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

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
        initAuthToken(apiKey)
    }

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

    @Throws(IllegalStateException::class)
    @JvmStatic
    fun preview(surveyId: String) {
        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.preview(). Make sure you call Iterate.init() before calling preview, see README for details")
        }
        iterateRepository.setPreviewSurveyId(surveyId)
    }

    @Throws(IllegalStateException::class)
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
        val tracking = if (lastUpdated != null) {
            TrackingContext(lastUpdated)
        } else {
            null
        }

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
                version = BuildConfig.VERSION_NAME,
                urlScheme = null // TODO: add urlScheme for deep link
            ),
            event = EventContext(eventName),
            type = EmbedType.MOBILE,
            targeting = targeting,
            tracking = tracking,
            userTraits = userTraits
        )

        // Call embed API
        iterateRepository.embed(
            embedContext,
            object : ApiResponseCallback<EmbedResults> {
                override fun onSuccess(result: EmbedResults) {
                    // Set the user auth token if one is returned
                    result.auth?.token?.let { token ->
                        iterateRepository.setUserAuthToken(token)
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
                                    showSurveyOrPrompt(survey, responseId, supportFragmentManager)
                                }
                            } else {
                                showSurveyOrPrompt(survey, responseId, supportFragmentManager)
                            }
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Log.e("sendEvent error", e.toString())
                }
            }
        )
    }

    @JvmStatic
    fun onResponse(
        userOnResponseCallback: (
            response: Response,
            question: Question,
            survey: Survey
        ) -> Unit
    ) {
        InteractionEventCallbacks.onResponse = userOnResponseCallback
    }

    @JvmStatic
    fun onEvent(
        userOnEventCallback: (
            type: InteractionEventTypes,
            data: InteractionEventData,
        ) -> Unit
    ) {
        InteractionEventCallbacks.onEvent = userOnEventCallback
    }

    private fun initAuthToken(companyAuthToken: String) {
        iterateRepository.setCompanyAuthToken(companyAuthToken)

        val userAuthToken = iterateRepository.getUserAuthToken()
        if (userAuthToken != null) {
            iterateRepository.setUserAuthToken(userAuthToken)
            iterateRepository.setApiKey(userAuthToken)
        }
    }

    private fun showSurveyOrPrompt(
        survey: Survey,
        responseId: Long,
        supportFragmentManager: FragmentManager
    ) {
        if (survey.prompt != null) {
            showPrompt(survey, responseId, supportFragmentManager)
        } else {
            showSurvey(survey, responseId, supportFragmentManager)
        }
        iterateRepository.displayed(survey)
    }

    private fun showSurvey(
        survey: Survey,
        responseId: Long,
        supportFragmentManager: FragmentManager
    ) {
        val authToken =
            iterateRepository.getUserAuthToken() ?: iterateRepository.getCompanyAuthToken()
        val eventTraits = iterateRepository.getEventTraits(responseId)
        SurveyView.newInstance(survey, authToken, eventTraits).apply {
            setListener(object : SurveyView.SurveyListener {
                override fun onDismiss(
                    source: InteractionEventSource,
                    progress: ProgressEventMessageData?
                ) {
                    dismissed(source, survey, progress)
                }
            })
            show(supportFragmentManager, null)
        }

        InteractionEvents.surveyDisplayed(survey)
    }

    private fun showPrompt(
        survey: Survey,
        responseId: Long,
        supportFragmentManager: FragmentManager
    ) {
        PromptView.newInstance(survey).apply {
            setListener(object : PromptView.PromptListener {
                override fun onDismiss(
                    source: InteractionEventSource,
                    progress: ProgressEventMessageData?
                ) {
                    dismissed(source, survey, progress)
                }

                override fun onPromptButtonClick(survey: Survey) {
                    showSurvey(survey, responseId, supportFragmentManager)
                }
            })
            show(supportFragmentManager, null)
        }

        InteractionEvents.promptDisplayed(survey)
    }

    private fun dismissed(
        source: InteractionEventSource,
        survey: Survey,
        progress: ProgressEventMessageData?
    ) {
        iterateRepository.dismissed(survey)
        InteractionEvents.dismiss(source, survey, progress)
    }
}
