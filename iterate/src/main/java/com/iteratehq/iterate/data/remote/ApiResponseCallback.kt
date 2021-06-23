package com.iteratehq.iterate.data.remote

/**
 * Generic interface for an API operation callback that either returns a result [T] or an [Exception]
 */
interface ApiResponseCallback<in T> {
    fun onSuccess(result: T)
    fun onError(e: Exception)
}
