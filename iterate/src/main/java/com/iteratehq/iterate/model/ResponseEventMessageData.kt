package com.iteratehq.iterate.model

import androidx.annotation.Keep

@Keep
data class ResponseEventMessageData(
    val question: Question?,
    val response: Response?
)
