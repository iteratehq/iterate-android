package com.iteratehq.iterate

import android.content.Context
import android.os.Build
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
import com.iteratehq.iterate.model.TriggerContext
import com.iteratehq.iterate.model.TriggerContextType
import com.iteratehq.iterate.model.TriggerType
import com.iteratehq.iterate.model.UserTraits
import com.iteratehq.iterate.view.PromptView
import com.iteratehq.iterate.view.SurveyView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

object Iterate {
    private lateinit var iterateRepository: IterateRepository
    private lateinit var apiKey: String
    private var urlScheme: String? = null
    private var surveyTextFontAssetPath: String? = null
    private var buttonFontAssetPath: String? = null

    /**
     * Minimal initialization that is expected to be called on app boot.
     *
     * @param context Activity or application context
     * @param apiKey Iterate API key
     * @param urlScheme Optional URL scheme used for the app deep link
     * @param surveyTextFontAssetPath Optional path to a font file relative to the assets folder to use for question prompts and other survey text
     * @param buttonFontAssetPath Optional path to a font file relative to the assets folder to use for survey interface buttons
     * @param useEncryptedSharedPreferences Option to use EncryptedSharedPreferences, default to true
     */
    @JvmStatic
    @JvmOverloads
    fun init(
        context: Context,
        apiKey: String,
        urlScheme: String? = null,
        surveyTextFontAssetPath: String? = null,
        buttonFontAssetPath: String? = null,
        useEncryptedSharedPreferences: Boolean = true,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            this@Iterate.iterateRepository = DefaultIterateRepository(
                context.applicationContext,
                apiKey,
                useEncryptedSharedPreferences
            )
            this@Iterate.apiKey = apiKey
            this@Iterate.urlScheme = urlScheme
            this@Iterate.surveyTextFontAssetPath = surveyTextFontAssetPath
            this@Iterate.buttonFontAssetPath = buttonFontAssetPath
            initAuthToken(apiKey)
        }
    }

    /**
     * Set the user traits to be included when sending events. Commonly called on login.
     *
     * @param userTraits The user traits
     * @throws IllegalStateException Failure when this function is called before calling the [init] function
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
     * Function to bypass all the restrictions imposed when showing a survey. An example is to always
     * show the survey, even though it is set to be only shown once in a month. This function is for
     * developer usage only, not for the real usage.
     *
     * @param surveyId The survey ID
     * @throws IllegalStateException Failure when this function is called before calling the [init] function
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun preview(surveyId: String) {
        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.preview(). Make sure you call Iterate.init() before calling preview, see README for details")
        }
        iterateRepository.setPreviewSurveyId(surveyId)
    }

    fun sendEvent(
        eventName: String,
        fragmentManager: FragmentManager,
        eventTraits: EventTraits? = null,
    ) {
        send(fragmentManager, eventContext = EventContext(eventName), eventTraits = eventTraits)
    }

    fun install(
        surveyId: String,
        fragmentManager: FragmentManager,
        eventTraits: EventTraits? = null
    ) {
        send(fragmentManager, triggerContext = TriggerContext(surveyId, TriggerContextType.MANUAL))
    }

    /**
     * Send an event to the server.
     *
     * @param eventName The event name
     * @param fragmentManager FragmentManager for displaying a prompt or survey UI.
     * @param eventTraits Optional The event traits
     * @throws IllegalStateException Failure when this function is called before calling the [init] function
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    @JvmOverloads
    fun send(
        fragmentManager: FragmentManager,
        triggerContext: TriggerContext? = null,
        eventContext: EventContext? = null,
        eventTraits: EventTraits? = null,
    ) {
        if (android.os.Build.VERSION.SDK_INT <= 24) {
            return
        }

        if (!::iterateRepository.isInitialized) {
            throw IllegalStateException("Error calling Iterate.send(). Make sure you call Iterate.init() before calling sendEvent, see README for details")
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
                urlScheme = urlScheme
            ),
            event = eventContext,
            type = EmbedType.MOBILE,
            targeting = targeting,
            tracking = tracking,
            trigger = triggerContext,
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
                        // Generate a unique id (current timestamp) for this survey display so we ensure
                        // we associate the correct event traits with it
                        val responseId = Date().time

                        // Initialize empty event traits
                        val finalEventTraits = EventTraits()

                        // If user provided event traits, add them first
                        if (eventTraits != null) {
                            finalEventTraits.putAll(eventTraits)
                        }

                        // If server returned event traits, add them (overwriting any duplicates)
                        if (result.eventTraits != null) {
                            finalEventTraits.putAll(result.eventTraits)
                        }

                        // Store the final event traits if we have any
                        if (finalEventTraits.isNotEmpty()) {
                            iterateRepository.setEventTraits(finalEventTraits, responseId)
                        }

                        getPreferredLanguage(survey)

                        // If the survey has a timer trigger, wait that number of seconds before showing the survey
                        if (
                            !result.triggers.isNullOrEmpty() &&
                            result.triggers[0].type == TriggerType.SECONDS
                        ) {
                            CoroutineScope(Dispatchers.Default).launch {
                                val seconds = result.triggers[0].options.seconds ?: 0
                                delay(seconds * 1000L)
                                showSurveyOrPrompt(survey, responseId, fragmentManager)
                            }
                        } else {
                            showSurveyOrPrompt(survey, responseId, fragmentManager)
                        }
                    }
                }

                override fun onError(e: Exception) {
                    Log.e("sendEvent error", e.toString())
                }
            }
        )
    }

    /**
     * Set the callback function to be invoked when there is a response.
     *
     * @param userOnResponseCallback The callback function
     */
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

    /**
     * Set the callback function to be invoked when there is an event. Common examples include the
     * events when prompt is displayed, survey is completed, or when a prompt or survey is dismissed.
     *
     * @param userOnEventCallback The callback function
     */
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
        fragmentManager: FragmentManager
    ) {
        if (survey.prompt != null) {
            showPrompt(survey, responseId, fragmentManager)
        } else {
            showSurvey(survey, responseId, fragmentManager)
        }
        iterateRepository.displayed(survey)
    }

    private fun showSurvey(
        survey: Survey,
        responseId: Long,
        fragmentManager: FragmentManager
    ) {
        val authToken =
            iterateRepository.getUserAuthToken() ?: iterateRepository.getCompanyAuthToken()
        val eventTraits = iterateRepository.getEventTraits(responseId)
        SurveyView.newInstance(survey, authToken, eventTraits, this.surveyTextFontAssetPath, this.buttonFontAssetPath).apply {
            setListener(object : SurveyView.SurveyListener {
                override fun onDismiss(
                    source: InteractionEventSource,
                    progress: ProgressEventMessageData?
                ) {
                    dismissed(source, survey, progress)
                }
            })
            try {
                if (canShowFragment(fragmentManager)) {
                    show(fragmentManager, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        InteractionEvents.surveyDisplayed(survey)
    }

    private fun showPrompt(
        survey: Survey,
        responseId: Long,
        fragmentManager: FragmentManager
    ) {
        PromptView.newInstance(survey, this.surveyTextFontAssetPath, this.buttonFontAssetPath).apply {
            setListener(object : PromptView.PromptListener {
                override fun onDismiss(
                    source: InteractionEventSource,
                    progress: ProgressEventMessageData?
                ) {
                    dismissed(source, survey, progress)
                }

                override fun onPromptButtonClick(survey: Survey) {
                    showSurvey(survey, responseId, fragmentManager)
                }
            })
            try {
                if (canShowFragment(fragmentManager)) {
                    show(fragmentManager, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun availableLanguages(survey: Survey): List<String> {
        val primaryLanguage = survey.primaryLanguage
        val languages = mutableListOf<String>()
        survey.translations?.forEach { translation ->
            languages.add(translation.language)
        }

        if (primaryLanguage != null) {
            languages.add(primaryLanguage)
        }

        return languages
    }

    private fun getPreferredLanguage(survey: Survey): String {
        val deviceLanguage = Locale.getDefault().language
        val available = availableLanguages(survey)

        val userTraits = iterateRepository.getUserTraits()
        val userTraitLanguage = userTraits?.get("language")?.toString()
        if (userTraitLanguage != null && available.contains(userTraitLanguage)) {
            return userTraitLanguage
        }

        if (available.contains(deviceLanguage)) {
            return deviceLanguage
        }

        return "en"
    }

    internal fun getTranslationForKey(key: String, survey: Survey): String? {
        val preferredLanguage = getPreferredLanguage(survey)
        survey.translations?.forEach { translation ->
            if (translation.language == preferredLanguage) {
                return translation.items?.get(key)?.text
            }
        }

        return null
    }

    private fun canShowFragment(fragmentManager: FragmentManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            !fragmentManager.isDestroyed && !fragmentManager.isStateSaved
        } else {
            !fragmentManager.isDestroyed
        }
    }
}
