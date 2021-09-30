package com.iteratehq.iterate.model

import androidx.annotation.Keep

@Keep
data class Question(
    val id: String,
    val prompt: String
)
