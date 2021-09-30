package com.iteratehq.iterate.model

import androidx.annotation.Keep

@Keep
enum class InteractionEventTypes(val value: String) {
    DISMISS("dismiss"),
    DISPLAYED("displayed"),
    RESPONSE("response"),
    SURVEY_COMPLETE("survey-complete"),
}
