package com.iteratehq.iterate.data.remote.model

internal data class ApiResponse<T>(
    val errors: List<ApiResponseError>?,
    val error: Any?,
    val results: T?
)
