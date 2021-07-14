package com.iteratehq.iterate

import com.iteratehq.iterate.model.InteractionEventDismissData
import com.iteratehq.iterate.model.InteractionEventDisplayedData
import com.iteratehq.iterate.model.InteractionEventResponseData
import com.iteratehq.iterate.model.InteractionEventSource
import com.iteratehq.iterate.model.InteractionEventSurveyCompleteData
import com.iteratehq.iterate.model.InteractionEventTypes
import com.iteratehq.iterate.model.ProgressEventMessageData
import com.iteratehq.iterate.model.Question
import com.iteratehq.iterate.model.Response
import com.iteratehq.iterate.model.Survey

object InteractionEvents {
    internal fun dismiss(
        source: InteractionEventSource,
        survey: Survey,
        progress: ProgressEventMessageData?
    ) {
        InteractionEventCallbacks.onEvent?.invoke(
            InteractionEventTypes.DISMISS,
            InteractionEventDismissData(progress, source, survey)
        )
    }

    internal fun promptDisplayed(survey: Survey) {
        InteractionEventCallbacks.onEvent?.invoke(
            InteractionEventTypes.DISPLAYED,
            InteractionEventDisplayedData(InteractionEventSource.PROMPT, survey)
        )
    }

    internal fun response(survey: Survey, response: Response?, question: Question?) {
        if (response != null && question != null) {
            InteractionEventCallbacks.onResponse?.invoke(response, question, survey)
            InteractionEventCallbacks.onEvent?.invoke(
                InteractionEventTypes.RESPONSE,
                InteractionEventResponseData(response, question, survey)
            )
        }
    }

    internal fun surveyComplete(survey: Survey) {
        InteractionEventCallbacks.onEvent?.invoke(
            InteractionEventTypes.SURVEY_COMPLETE,
            InteractionEventSurveyCompleteData(survey)
        )
    }

    internal fun surveyDisplayed(survey: Survey) {
        InteractionEventCallbacks.onEvent?.invoke(
            InteractionEventTypes.DISPLAYED,
            InteractionEventDisplayedData(InteractionEventSource.SURVEY, survey)
        )
    }
}
