package com.iteratehq.iterate.data.remote

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets

internal fun MockWebServer.enqueueResponse(
    fileName: String,
    code: Int,
) {
    val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
    inputStream?.source()?.buffer()?.let { source ->
        enqueue(
            MockResponse()
                .setBody(source.readString(StandardCharsets.UTF_8))
                .setResponseCode(code),
        )
    }
}
