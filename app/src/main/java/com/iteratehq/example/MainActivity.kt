package com.iteratehq.example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iteratehq.example.databinding.ActivityMainBinding
import com.iteratehq.iterate.Iterate
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.UserTraits
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButtonHandlers()

        Iterate.onResponse { response, question, survey ->
            Log.d("onResponseCallback", "$response $question $survey")
        }

        Iterate.onEvent { type, data ->
            Log.d("onEventCallback", "$type $data")
        }

        Iterate.init(this, API_KEY)

        // TODO: implement Iterate.preview
    }

    private fun setupButtonHandlers() {
        binding.btnTriggerSurvey.setOnClickListener {

            Iterate.sendEvent(
                "show-survey-button-tapped",
                EventTraits("currentTime" to Date().time),
                supportFragmentManager
            )
        }

        binding.btnLogin.setOnClickListener {
            Iterate.identify(
                UserTraits(
                    "external_id" to EXTERNAL_ID,
                    "email" to EMAIL
                )
            )
        }

        binding.btnLogout.setOnClickListener {
            Iterate.reset()
        }
    }

    companion object {
        private const val EXTERNAL_ID = "user-123"
        private const val EMAIL = "exampl@email.com"
        private const val API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55X2lkIjoiNWRmZTM2OGEwOWI2ZWYwMDAxYjNlNjE4IiwiaWF0IjoxNTc2OTQxMTk0fQ.QBWr2goMwOngVhi6wY9sdFAKEvBGmn-JRDKstVMFh6M"
    }
}
