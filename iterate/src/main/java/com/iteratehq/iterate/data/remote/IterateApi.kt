package com.iteratehq.iterate.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.data.remote.model.ApiResponse
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.Survey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

internal interface IterateApi {
    fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>?
    )

    fun displayed(
        survey: Survey,
        callback: ApiResponseCallback<EmbedResults>?
    )

    fun dismissed(
        survey: Survey,
        callback: ApiResponseCallback<EmbedResults>?
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
        callback: ApiResponseCallback<EmbedResults>?
    ) {
        executeAsync(callback) {
            val path = "/surveys/${survey.id}/displayed"
            httpRequest(path, Method.POST, survey)
        }
    }

    override fun dismissed(
        survey: Survey,
        callback: ApiResponseCallback<EmbedResults>?
    ) {
        executeAsync(callback) {
            val path = "/surveys/${survey.id}/dismiss"
            httpRequest(path, Method.POST, survey)
        }
    }

    private suspend inline fun <T, reified R> httpRequest(
        path: String,
        method: Method,
        body: T
    ): ApiResponse<R> {
        return withContext(workContext) {
            val url = URL("$apiHost/api/v1$path")
            val conn = (url.openConnection() as HttpsURLConnection).apply {
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                requestMethod = method.value
                doOutput = (method == Method.POST)
            }

            val gson = Gson()
            val bodyJson = gson.toJson(body)
            conn.outputStream.use { os ->
                val input = bodyJson.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode
            if (code < 200 || code >= 300) {
                throw Exception("Error calling API. Received HTTP status code $code")
            }

            val response = StringBuilder()
            BufferedReader(InputStreamReader(conn.inputStream, "utf-8")).use { br ->
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
        }
    }

    private fun <T> executeAsync(
        callback: ApiResponseCallback<T>? = null,
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

    private companion object {
        private const val DEFAULT_HOST = "https://iteratehq.com"
    }
}

internal enum class Method(val value: String) {
    GET("GET"),
    POST("POST")
}
