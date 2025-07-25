package com.iteratehq.example

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.iteratehq.example.databinding.ActivityMainBinding
import com.iteratehq.iterate.Iterate
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.UserTraits
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge and set status bar icons to dark
        enableEdgeToEdge()
        WindowCompat
            .getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom,
            )
            insets
        }

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
