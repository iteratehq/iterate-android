package com.iteratehq.iterate

import com.iteratehq.iterate.model.InteractionEventData
import com.iteratehq.iterate.model.InteractionEventTypes
import com.iteratehq.iterate.model.Question
import com.iteratehq.iterate.model.Response
import com.iteratehq.iterate.model.Survey

object InteractionEventCallbacks {
    var onResponse: (
        (
            response: Response,
            question: Question,
            survey: Survey,
        ) -> Unit
    )? = null

    var onEvent: (
        (
            type: InteractionEventTypes,
            data: InteractionEventData,
        ) -> Unit
    )? = null
}
