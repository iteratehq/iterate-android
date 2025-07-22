package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TargetingContext(
    val frequency: Frequency?,
    @SerializedName("survey_id")
    val surveyId: String?,
)

@Keep
enum class Frequency(
    val value: String,
) {
    @SerializedName("always")
    ALWAYS("always"),
}
