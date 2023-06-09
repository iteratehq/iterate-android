package com.iterate.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.iterate.myapplication.ui.theme.IterateTheme
import com.iteratehq.iterate.Iterate
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.UserTraits
import java.time.LocalDateTime

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Iterate.init(this, API_KEY)
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

        setContent {
            IterateTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                Iterate.sendEvent(
                                    "show-survey-button-tapped",
                                    this@MainActivity.supportFragmentManager,
                                    EventTraits("currentDate" to LocalDateTime.now())
                                )
                            }
                        ) {
                            Text(text = "Show Survey")
                        }

                        Button(
                            onClick = {
                                Iterate.identify(
                                    UserTraits(
                                        "external_id" to EXTERNAL_ID,
                                        "email" to EMAIL,
                                        "date_joined" to LocalDateTime.of(2023, 1, 1, 1, 1, 1)
                                    )
                                )
                            }
                        ) {
                            Text(text = "Login")
                        }

                        Button(
                            onClick = {
                                Iterate.reset()
                            }
                        ) {
                            Text(text = "Logout")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val API_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55X2lkIjoiNWRmZTM2OGEwOWI2ZWYwMDAxYjNlNjE4IiwiaWF0IjoxNTc2OTQxMTk0fQ.QBWr2goMwOngVhi6wY9sdFAKEvBGmn-JRDKstVMFh6M"
        private const val EMAIL = "example@email.com"
        private const val EXTERNAL_ID = "user-123"
        private const val SURVEY_ID = "5efa0121a9fffa0001c70b8d"
    }
}
