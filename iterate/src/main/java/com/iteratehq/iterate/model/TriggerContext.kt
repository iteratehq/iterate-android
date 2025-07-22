package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TriggerContext(
    @SerializedName("survey_id")
    val surveyId: String?,
    val type: TriggerContextType?,
)

@Keep
enum class TriggerContextType(
    val value: String,
) {
    @SerializedName("manual")
    MANUAL("manual"),
}
