package com.iteratehq.example

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.iterable.iterableapi.AuthFailure
import com.iterable.iterableapi.IterableAction
import com.iterable.iterableapi.IterableActionContext
import com.iterable.iterableapi.IterableApi
import com.iterable.iterableapi.IterableAuthHandler
import com.iterable.iterableapi.IterableConfig
import com.iterable.iterableapi.IterableCustomActionHandler
import com.iterable.iterableapi.IterableEmbeddedUpdateHandler
import com.iterable.iterableapi.IterableUrlHandler
import com.iteratehq.example.databinding.ActivityMainBinding
import com.iteratehq.iterate.Iterate
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.UserTraits
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity(), IterableUrlHandler, IterableCustomActionHandler {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtonHandlers()

        Iterate.init(this, API_KEY, null, "Merriweather-Regular.ttf", "WorkSans-VariableFont_wght.ttf")

        Iterate.onResponse { response, question, survey ->
            Log.d("onResponseCallback", "$response $question $survey")
        }

        Iterate.onEvent { type, data ->
            Log.d("onEventCallback", "$type $data")
        }

        val isPreviewEnabled = false
        if (isPreviewEnabled) {
            Iterate.preview(SURVEY_ID)
        }

        val config =
            IterableConfig.Builder()
                .setAuthHandler(
                    object : IterableAuthHandler {
                        override fun onAuthTokenRequested(): String {
                            // This is a pre-generated JWT token for the test user
                            // Note it expires on 3/15/2026
                            return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3R1c2VyQGl0" +
                                "ZXJhdGVocS5jb20iLCJpYXQiOjE3NDQ3NDU5MzYsImV4cCI6MTc3MzYwMzUzNn0.N7JK" +
                                "iZX_I_SboMGmZ5Vu4vfYDiec0LEgk1JefRwQBT0"
                        }

                        override fun onTokenRegistrationSuccessful(authToken: String) {}

                        override fun onAuthFailure(authFailure: AuthFailure) {}
                    },
                ).setUrlHandler(this)
                .setCustomActionHandler(this)
                .setEnableEmbeddedMessaging(true)
                .build()

        val jwtAuthToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3R1c2VyQGl0" +
                "ZXJhdGVocS5jb20iLCJpYXQiOjE3NDQ3NDU5MzYsImV4cCI6MTc3MzYwMzUzNn0.N7JK" +
                "iZX_I_SboMGmZ5Vu4vfYDiec0LEgk1JefRwQBT0"

        // TODO: Insert real Iterable apiKey when testing
        IterableApi.initialize(this, "YOUR KEY", config)
        IterableApi.getInstance().setEmail("testuser@iteratehq.com", jwtAuthToken)

        IterateIterableMessageHandler.initialize(1389, supportFragmentManager)
    }

    override fun handleIterableURL(
        uri: Uri,
        actionContext: IterableActionContext,
    ): Boolean {
        return false
    }

    override fun handleIterableCustomAction(
        action: IterableAction,
        actionContext: IterableActionContext,
    ): Boolean {
        return false
    }

    private fun setupButtonHandlers() {
        binding.btnTriggerSurvey.setOnClickListener {
            Iterate.sendEvent(
                "show-survey-button-tapped",
                supportFragmentManager,
                EventTraits("currentDate" to Date()),
            )
        }

        binding.btnLogin.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, 2023)
            calendar.set(Calendar.MONTH, Calendar.JANUARY) // Month is 0-based
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val januaryFirst2023: Date = calendar.time

            Iterate.identify(
                UserTraits(
                    "external_id" to EXTERNAL_ID,
                    "email" to EMAIL,
                    "date_joined" to januaryFirst2023,
                ),
            )
        }

        binding.btnLogout.setOnClickListener {
            Iterate.reset()
        }
    }

    companion object {
        private const val API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55X2lkIjoiN" +
                "WRmZTM2OGEwOWI2ZWYwMDAxYjNlNjE4IiwiaWF0IjoxNTc2OTQxMTk0fQ.QBW" +
                "r2goMwOngVhi6wY9sdFAKEvBGmn-JRDKstVMFh6M"
        private const val EMAIL = "example@email.com"
        private const val EXTERNAL_ID = "user-123"
        private const val SURVEY_ID = "5efa0121a9fffa0001c70b8d"
    }
}

object IterateIterableMessageHandler : IterableEmbeddedUpdateHandler {
    private var fragmentManager: FragmentManager? = null
    private var placementId: Long = 0

    fun initialize(
        placementId: Long,
        fragmentManager: FragmentManager,
    ) {
        this.placementId = placementId
        this.fragmentManager = fragmentManager
        IterableApi.getInstance().embeddedManager.addUpdateListener(this)
    }

    override fun onMessagesUpdated() {
        val fm = fragmentManager ?: return

        IterableApi.getInstance().embeddedManager.getMessages(placementId)
            ?.firstOrNull { it.payload?.get("type") == "iterate_survey" }
            ?.payload?.get("survey_id")?.let { surveyId ->
                if (surveyId is String) {
                    Iterate.install(surveyId, fm)
                }
            }
    }

    override fun onEmbeddedMessagingDisabled() {}
}
