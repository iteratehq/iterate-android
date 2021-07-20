package com.iteratehq.iterate.model

sealed interface InteractionEventData

data class InteractionEventResponseData(
    val response: Response,
    val question: Question,
    val survey: Survey
) : InteractionEventData

data class InteractionEventDismissData(
    val progress: ProgressEventMessageData?,
    val source: InteractionEventSource,
    val survey: Survey
) : InteractionEventData

data class InteractionEventSurveyCompleteData(
    val survey: Survey
) : InteractionEventData

data class InteractionEventDisplayedData(
    val source: InteractionEventSource,
    val survey: Survey
) : InteractionEventData

data class ProgressEventMessageData(
    val completed: Int,
    val total: Int,
    val currentQuestion: Question?
)

enum class InteractionEventSource(val value: String) {
    PROMPT("prompt"),
    SURVEY("survey")
}
