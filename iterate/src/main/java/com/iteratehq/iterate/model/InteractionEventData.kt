package com.iteratehq.iterate.model

import androidx.annotation.Keep

@Keep
sealed interface InteractionEventData

@Keep
data class InteractionEventResponseData(
    val response: Response,
    val question: Question,
    val survey: Survey,
) : InteractionEventData

@Keep
data class InteractionEventDismissData(
    val progress: ProgressEventMessageData?,
    val source: InteractionEventSource,
    val survey: Survey,
) : InteractionEventData

@Keep
data class InteractionEventSurveyCompleteData(
    val survey: Survey,
) : InteractionEventData

@Keep
data class InteractionEventDisplayedData(
    val source: InteractionEventSource,
    val survey: Survey,
) : InteractionEventData

@Keep
data class ProgressEventMessageData(
    val completed: Int,
    val total: Int,
    val currentQuestion: Question?,
)

@Keep
enum class InteractionEventSource(
    val value: String,
) {
    PROMPT("prompt"),
    SURVEY("survey"),
}
