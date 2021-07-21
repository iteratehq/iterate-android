package com.iteratehq.iterate.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.data.remote.model.ApiResponse
import com.iteratehq.iterate.model.DismissedResults
import com.iteratehq.iterate.model.DisplayedResults
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.Survey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

internal interface IterateApi {
    fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>? = null
    )

    fun displayed(
        survey: Survey,
        callback: ApiResponseCallback<DisplayedResults>? = null
    )

    fun dismissed(
        survey: Survey,
        callback: ApiResponseCallback<DismissedResults>? = null
    )
}

internal class DefaultIterateApi(
    private val apiKey: String,
    private val apiHost: String = DEFAULT_HOST,
    private val workContext: CoroutineContext = Dispatchers.IO
) : IterateApi {

    override fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>?
    ) {
        executeAsync(callback) {
            val path = "/surveys/embed"
            httpRequest(path, Method.POST, embedContext)
        }
    }

    override fun displayed(
        survey: Survey,
        callback: ApiResponseCallback<DisplayedResults>?
    ) {
        executeAsync(callback) {
            val path = "/surveys/${survey.id}/displayed"
            httpRequest(path, Method.POST, Any())
        }
    }

    override fun dismissed(
        survey: Survey,
        callback: ApiResponseCallback<DismissedResults>?
    ) {
        executeAsync(callback) {
            val path = "/surveys/${survey.id}/dismiss"
            httpRequest(path, Method.POST, Any())
        }
    }

    private suspend inline fun <T, reified R> httpRequest(
        path: String,
        method: Method,
        body: T
    ): ApiResponse<R> {
        return withContext(workContext) {
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("$apiHost/api/v1$path")
                urlConnection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    requestMethod = method.value
                    doOutput = (method == Method.POST)
                }

                val gson = Gson()
                val bodyJson = gson.toJson(body)
                urlConnection.outputStream.use { os ->
                    val input = bodyJson.toByteArray(charset("utf-8"))
                    os.write(input, 0, input.size)
                }

                val code = urlConnection.responseCode
                if (code < 200 || code >= 300) {
                    throw Exception("Error calling API. Received HTTP status code $code")
                }

                val response = StringBuilder()
                BufferedReader(InputStreamReader(urlConnection.inputStream, "utf-8")).use { br ->
                    var responseLine = br.readLine()
                    while (responseLine != null) {
                        response.append(responseLine.trim())
                        responseLine = br.readLine()
                    }
                }

                val type = TypeToken
                    .getParameterized(ApiResponse::class.java, R::class.java)
                    .type
                gson.fromJson(response.toString(), type)
            } finally {
                urlConnection?.disconnect()
            }
        }
    }

    /**
     * Execute the given [apiCall] function asynchronously and return the result through a [callback].
     *
     * [CoroutineScope] with the default context of [Dispatchers.IO] is used to run the [apiCall].
     * The [runCatching] will return either [Result.success] if the [apiCall] operation is successful,
     * or [Result.failure] if there is any exception thrown. The [result] will then be dispatched
     * back to the main UI thread through [Dispatchers.Main] context, either through [onSuccess] if
     * it is a success or [onFailure] if it is a failure.
     */
    private fun <T> executeAsync(
        callback: ApiResponseCallback<T>?,
        apiCall: suspend () -> ApiResponse<T>?
    ) {
        CoroutineScope(workContext).launch {
            val result = runCatching {
                requireNotNull(apiCall())
            }
            dispatchResult(result, callback)
        }
    }

    private suspend fun <T> dispatchResult(
        result: Result<ApiResponse<T>>,
        callback: ApiResponseCallback<T>?
    ) = withContext(Dispatchers.Main) {
        if (callback != null) {
            result.fold(
                onSuccess = {
                    when {
                        it.results != null -> callback.onSuccess(it.results)
                        it.errors != null -> callback.onError(Exception(it.errors.joinToString("\n")))
                        it.error != null -> callback.onError(Exception(it.error.toString()))
                        else -> callback.onError(Exception("Invalid response"))
                    }
                },
                onFailure = {
                    callback.onError(Exception(it.message, it))
                }
            )
        }
    }

    companion object {
        const val DEFAULT_HOST = "https://iteratehq.com"
    }
}

internal enum class Method(val value: String) {
    GET("GET"),
    POST("POST")
}
