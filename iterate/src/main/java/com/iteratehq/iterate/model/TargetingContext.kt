package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

data class TargetingContext(
    val frequency: Frequency?,
    @SerializedName("survey_id")
    val surveyId: String?
)

enum class Frequency(val value: String) {
    ALWAYS("always")
}
