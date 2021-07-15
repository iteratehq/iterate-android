package com.iteratehq.iterate.view

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.iteratehq.iterate.data.remote.DefaultIterateApi
import com.iteratehq.iterate.databinding.SurveyViewBinding
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.Survey


class SurveyView : DialogFragment() {

    private lateinit var binding: SurveyViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SurveyViewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setupView() {
        val survey = arguments?.getParcelable<Survey>(SURVEY)
        val authToken = arguments?.getString(AUTH_TOKEN)
        val eventTraits = arguments?.getSerializable(EVENT_TRAITS) as EventTraits?

        val params = mutableListOf<String>()

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
        val theme = if (isDarkThemeOn()) "dark" else "light"
        params.add("theme=$theme")

        val url = "${DefaultIterateApi.DEFAULT_HOST}/${survey?.companyId}/${survey?.id}/mobile?${
            params.joinToString("&")
        }"

        binding.webview.run {
            settings.javaScriptEnabled = true
            loadUrl(url)
        }
    }

    private fun isDarkThemeOn(): Boolean {
        return (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
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
