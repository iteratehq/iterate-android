package com.iteratehq.iterate.view

import android.content.DialogInterface
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.InteractionEvents
import com.iteratehq.iterate.R
import com.iteratehq.iterate.data.remote.DefaultIterateApi
import com.iteratehq.iterate.databinding.SurveyViewBinding
import com.iteratehq.iterate.model.EventMessageTypes
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.InteractionEventSource
import com.iteratehq.iterate.model.ProgressEventMessageData
import com.iteratehq.iterate.model.ResponseEventMessageData
import com.iteratehq.iterate.model.Survey

class SurveyView : DialogFragment() {

    interface SurveyListener {
        fun onDismiss(source: InteractionEventSource, progress: ProgressEventMessageData?)
    }

    private lateinit var binding: SurveyViewBinding
    private val survey by lazy { arguments?.getParcelable<Survey>(SURVEY)!! }
    private var listener: SurveyListener? = null
    private var progress: ProgressEventMessageData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SurveyViewBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_NoActionBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismiss(InteractionEventSource.SURVEY, progress)
    }

    fun setListener(listener: SurveyListener) {
        this.listener = listener
    }

    private fun setupView() {
        val params = mutableListOf<String>()
        val authToken = arguments?.getString(AUTH_TOKEN)
        val eventTraits = arguments?.getSerializable(EVENT_TRAITS) as EventTraits?

        // Add the auth token
        if (authToken != null) {
            params.add("auth_token=$authToken")
        }

        // Add the auth token
        eventTraits?.forEach { (key, value) ->
            if (value is Boolean) {
                params.add("response_boolean_$key=$value")
            } else if (value is Long || value is Int) {
                params.add("response_number_$key=$value")
            } else {
                params.add("response_$key=$value")
            }
        }

        // Add theme
        val theme = if (isDarkTheme()) "dark" else "light"
        params.add("theme=$theme")

        val url = "${DefaultIterateApi.DEFAULT_HOST}/${survey.companyId}/" +
            "${survey.id}/mobile?${params.joinToString("&")}"

        binding.webview.apply {
            // Set WebView background color with respect to the theme
            val color = if (isDarkTheme()) R.color.blackLayoutBackground else R.color.white
            setBackgroundColor(ContextCompat.getColor(requireContext(), color))

            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.isVisible = false
                }
            }

            // Bind an interface between JavaScript and Android code.
            // "ReactNativeWebView" is the interface name used when the JavaScript calls the
            // "postMessage" function.
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun postMessage(message: String) {
                        onMessage(message)
                    }
                },
                "ReactNativeWebView"
            )

            loadUrl(url)
        }
    }

    private fun isDarkTheme(): Boolean {
        return (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
    }

    private fun onMessage(message: String) {
        val gson = Gson()
        val messageMap = gson.fromJson<Map<String, Any?>>(
            message,
            object : TypeToken<Map<String, Any?>>() {}.type
        )

        when (messageMap["type"]) {
            EventMessageTypes.CLOSE.value -> {
                dismiss()
            }
            EventMessageTypes.PROGRESS.value -> {
                progress = gson.fromJson(
                    gson.toJson(messageMap["data"]),
                    ProgressEventMessageData::class.java
                )
            }
            EventMessageTypes.RESPONSE.value -> {
                val data = gson.fromJson(
                    gson.toJson(messageMap["data"]),
                    ResponseEventMessageData::class.java
                )
                InteractionEvents.response(survey, data.response, data.question)
            }
            EventMessageTypes.SURVEY_COMPLETE.value -> {
                InteractionEvents.surveyComplete(survey)
            }
        }
    }

    companion object {
        private const val SURVEY = "survey"
        private const val AUTH_TOKEN = "auth_token"
        private const val EVENT_TRAITS = "event_traits"

        fun newInstance(
            survey: Survey,
            authToken: String?,
            eventTraits: EventTraits?
        ): SurveyView {
            val bundle = Bundle().apply {
                putParcelable(SURVEY, survey)
                putString(AUTH_TOKEN, authToken)
                putSerializable(EVENT_TRAITS, eventTraits)
            }
            return SurveyView().apply {
                arguments = bundle
            }
        }
    }
}
