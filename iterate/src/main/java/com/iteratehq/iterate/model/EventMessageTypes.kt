package com.iteratehq.iterate.model

enum class EventMessageTypes(val value: String) {
    CLOSE("close"),
    PROGRESS("progress"),
    RESPONSE("response"),
    SURVEY_COMPLETE("survey-complete")
}
