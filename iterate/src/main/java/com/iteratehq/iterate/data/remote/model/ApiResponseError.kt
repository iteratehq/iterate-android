package com.iteratehq.iterate.data.remote.model

import com.google.gson.annotations.SerializedName

data class ApiResponseError(
    val code: Int,
    val message: String?,
    val type: String?,
    @SerializedName("user_message")
    val userMessage: String?
)
