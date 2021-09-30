package com.iteratehq.iterate.data.remote.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiResponseError(
    val code: Int,
    val message: String?,
    val type: String?,
    @SerializedName("user_message")
    val userMessage: String?
)
