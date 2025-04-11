package com.iteratehq.iterate.view

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.URL
import java.net.URLEncoder
import java.util.Date

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
        val surveyTextFont = arguments?.getString(SURVEY_TEXT_FONT)
        val buttonFont = arguments?.getString(BUTTON_FONT)

        // Add the auth token
        if (authToken != null) {
            params.add("auth_token=$authToken")
        }

        // Add the event traits
        eventTraits?.forEach { (key, value) ->
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
            if (value is Boolean) {
                params.add("response_boolean_$encodedKey=$encodedValue")
            } else if (value is Long || value is Int) {
                params.add("response_number_$encodedKey=$encodedValue")
            } else if (value is Date) {
                val timestamp = value.getTime() / 1000
                params.add("response_date_$encodedKey=$timestamp")
            } else {
                params.add("response_$encodedKey=$encodedValue")
            }
        }

        // Add theme
        val theme = if (isDarkTheme()) "dark" else "light"
        params.add("theme=$theme")
        params.add("absoluteURLs=true")

        if (surveyTextFont != null) {
            params.add("surveyTextFontPath=file:///android_asset/$surveyTextFont")
        }
        if (buttonFont != null) {
            params.add("buttonFontPath=file:///android_asset/$buttonFont")
        }

        val url = "${DefaultIterateApi.DEFAULT_HOST}/${survey.companyId}/" +
            "${survey.id}/mobile?${params.joinToString("&")}"
        binding.webview.apply {
            // Set WebView background color with respect to the theme
            val color = if (isDarkTheme()) R.color.blackLight else R.color.white
            setBackgroundColor(ContextCompat.getColor(requireContext(), color))

            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.isVisible = false
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val isIterateRequest = "${request?.url?.scheme}://${request?.url?.host}" == DefaultIterateApi.DEFAULT_HOST
                    if (isIterateRequest) {
                        return false
                    }

                    if (request?.url != null) {
                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                        view?.context?.startActivity(intent)
                        return true
                    } else {
                        return false
                    }
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

            var response = ""
            var error: Exception? = null
            runBlocking {
                try {
                    withTimeout(5000) {
                        launch(Dispatchers.IO) {
                            response = URL(url).readText()
                        }
                    }
                } catch (e: Exception) {
                    error = e
                }
            }

            if (error == null) {
                loadDataWithBaseURL(
                    "file:///?${params.joinToString("&")}",
                    response,
                    "text/html",
                    "utf-8",
                    ""
                )
            } else {
                dismiss()
            }
        }
    }

    private fun isDarkTheme(): Boolean {
        val survey = arguments?.getParcelable<Survey>(SURVEY)
        if (survey?.appearance == "dark") return true
        if (survey?.appearance == "light") return false
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
        private const val SURVEY_TEXT_FONT = "survey_text_font"
        private const val BUTTON_FONT = "button_font"

        fun newInstance(
            survey: Survey,
            authToken: String?,
            eventTraits: EventTraits?,
            surveyTextFont: String? = null,
            buttonFont: String? = null
        ): SurveyView {
            val bundle = Bundle().apply {
                putParcelable(SURVEY, survey)
                putString(AUTH_TOKEN, authToken)
                putSerializable(EVENT_TRAITS, eventTraits)
                putString(SURVEY_TEXT_FONT, surveyTextFont)
                putString(BUTTON_FONT, buttonFont)
            }
            return SurveyView().apply {
                arguments = bundle
            }
        }
    }
}
