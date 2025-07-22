package com.iteratehq.iterate.model

import androidx.annotation.Keep

@Keep
enum class EventMessageTypes(
    val value: String,
) {
    CLOSE("close"),
    PROGRESS("progress"),
    RESPONSE("response"),
    SURVEY_COMPLETE("survey-complete"),
}
