package com.iteratehq.iterate.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.Survey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.CoroutineContext

internal interface IterateApi {
    fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>
    )

    fun displayed(survey: Survey)
    fun dismissed(survey: Survey)
}

internal class DefaultIterateApi(
    private val apiHost: String,
    private val apiKey: String? = null,
    private val workContext: CoroutineContext = Dispatchers.IO
) : IterateApi {

    override fun embed(
        embedContext: EmbedContext,
        callback: ApiResponseCallback<EmbedResults>
    ) {
        executeAsync(callback) {
            val url = "$apiHost/surveys/embed"
            httpRequest(url, Method.POST, embedContext)
        }
    }

    override fun displayed(survey: Survey) {
        executeAsync<Survey> {
            val url = "$apiHost/surveys/${survey.id}/displayed"
            httpRequest(url, Method.POST, survey)
        }
    }

    override fun dismissed(survey: Survey) {
        executeAsync<Survey> {
            val url = "$apiHost/surveys/${survey.id}/dismiss"
            httpRequest(url, Method.POST, survey)
        }
    }

    private suspend fun <T, R> httpRequest(url: String, method: Method, body: T): R {
        return withContext(workContext) {
            val url = URL(url)
            val conn = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = method.value
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val gson = Gson()
            val os = conn.outputStream
            BufferedWriter(OutputStreamWriter(os, "UTF-8")).apply {
                write(gson.toJson(body))
                flush()
                close()
            }
            os.close()

            // Create request to the given URL
            conn.connect()

            val type = object : TypeToken<R>() {}.type
            gson.fromJson<R>(conn.responseMessage, type)
        }
    }

    private fun <T> executeAsync(
        callback: ApiResponseCallback<T>? = null,
        apiCall: suspend () -> T?
    ) {
        CoroutineScope(workContext).launch {
            val result = runCatching {
                requireNotNull(apiCall())
            }
            dispatchResult(result, callback)
        }
    }

    private suspend fun <T> dispatchResult(
        result: Result<T>,
        callback: ApiResponseCallback<T>?
    ) = withContext(Dispatchers.Main) {
        if (callback != null) {
            result.fold(
                onSuccess = {
                    callback.onSuccess(it)
                },
                onFailure = {
                    callback.onError(Exception(it.message))
                }
            )
        }
    }
}

internal enum class Method(val value: String) {
    GET("GET"),
    POST("POST")
}
