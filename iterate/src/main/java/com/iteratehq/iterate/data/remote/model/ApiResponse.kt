package com.iteratehq.iterate.data.remote.model

import androidx.annotation.Keep

@Keep
internal data class ApiResponse<T>(
    val errors: List<ApiResponseError>?,
    val error: Any?,
    val results: T?,
)
