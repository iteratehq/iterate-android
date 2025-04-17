package com.iteratehq.iterate.data.remote

import com.iteratehq.iterate.MainCoroutineRule
import com.iteratehq.iterate.model.Auth
import com.iteratehq.iterate.model.DismissedResults
import com.iteratehq.iterate.model.DisplayedResults
import com.iteratehq.iterate.model.EmbedContext
import com.iteratehq.iterate.model.EmbedResults
import com.iteratehq.iterate.model.Prompt
import com.iteratehq.iterate.model.Survey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class IterateApiTest {
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var server: MockWebServer

    @Before
    fun before() {
        server = MockWebServer()
    }

    @After
    fun after() {
        server.shutdown()
    }

    @Test
    fun `should have the right default host URL`() {
        val expectedUrl = "https://iteratehq.com"
        val defaultHostUrl = DefaultIterateApi.DEFAULT_HOST
        assertEquals(expectedUrl, defaultHostUrl)
    }

    @Test
    fun `should return a correct EmbedResults on successful response when calling embed API`() =
        runBlocking {
            // Create a CountDownLatch that allows one or more threads to wait until a set of operations
            // being performed in other threads completes
            val latch = CountDownLatch(1)

            server.enqueueResponse("embed.json", 200)
            server.start()

            val embedContext = EmbedContext(null, null, null, null, null, null, null)
            val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
            iterateApi.embed(
                embedContext,
                object : ApiResponseCallback<EmbedResults> {
                    override fun onSuccess(result: EmbedResults) {
                        val expectedEmbedResults = EmbedResults(
                            auth = Auth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55X2lkIjoiNWRmZTM2OGEwOWI2ZWYwMDAxYjNlNjE4IiwidXNlcl9pZCI6IjYwZjU0ZGIzMjc4NzVjMDAwMTVhNmNjZSIsImlhdCI6MTYyNjg1ODU0Nn0.TaQfldapD0jInoa0zY4vepwlBUun45U_5f-Qf5THq0g"),
                            survey = Survey(
                                id = "5efa0121a9fffa0001c70b8d",
                                companyId = "5dfe368a09b6ef0001b3e618",
                                title = "Integration Test Surveys (DO NOT DELETE)",
                                prompt = Prompt(
                                    message = "Help us improve the Iterate iOS SDK by answering a few questions about your ideal use cases",
                                    buttonText = "Happy to Help"
                                ),
                                color = "#0693e3",
                                colorDark = "#ffffff"
                            ),
                            triggers = emptyList(),
                            tracking = result.tracking,
                            eventTraits = null
                        )
                        assertEquals(expectedEmbedResults, result)
                        latch.countDown()
                    }

                    override fun onError(e: Exception) {
                        throw Exception("This callback should not be called")
                    }
                }
            )

            // Wait until the latch has counted down to zero or timed out
            val completed = latch.await(3, TimeUnit.SECONDS)
            assertTrue(completed)
        }

    @Test
    fun `should return an Exception on failed response when calling embed API`() = runBlocking {
        val latch = CountDownLatch(1)

        // Set the HTTP response code to 401
        server.enqueueResponse("embed.json", 401)
        server.start()

        val embedContext = EmbedContext(null, null, null, null, null, null, null)
        val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
        iterateApi.embed(
            embedContext,
            object : ApiResponseCallback<EmbedResults> {
                override fun onSuccess(result: EmbedResults) {
                    throw Exception("This callback should not be called")
                }

                override fun onError(e: Exception) {
                    latch.countDown()
                }
            }
        )

        val completed = latch.await(3, TimeUnit.SECONDS)
        assertTrue(completed)
    }

    @Test
    fun `should return a correct DisplayedResults on successful response when calling displayed API`() =
        runBlocking {
            val latch = CountDownLatch(1)

            server.enqueueResponse("displayed.json", 200)
            server.start()

            val survey = Survey("id", "companyId", "title", null, null, null)
            val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
            iterateApi.displayed(
                survey,
                object : ApiResponseCallback<DisplayedResults> {
                    override fun onSuccess(result: DisplayedResults) {
                        val expectedDisplayedResults = DisplayedResults(
                            id = "60f861338718c20001bee688",
                            lastDisplayed = "2021-07-21T18:02:27.534531401Z"
                        )
                        assertEquals(expectedDisplayedResults, result)
                        latch.countDown()
                    }

                    override fun onError(e: Exception) {
                        throw Exception("This callback should not be called")
                    }
                }
            )

            val completed = latch.await(3, TimeUnit.SECONDS)
            assertTrue(completed)
        }

    @Test
    fun `should return an Exception on failed response when calling displayed API`() = runBlocking {
        val latch = CountDownLatch(1)

        // Set the HTTP response code to 401
        server.enqueueResponse("displayed.json", 401)
        server.start()

        val survey = Survey("id", "companyId", "title", null, null, null)
        val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
        iterateApi.displayed(
            survey,
            object : ApiResponseCallback<DisplayedResults> {
                override fun onSuccess(result: DisplayedResults) {
                    throw Exception("This callback should not be called")
                }

                override fun onError(e: Exception) {
                    latch.countDown()
                }
            }
        )

        val completed = latch.await(3, TimeUnit.SECONDS)
        assertTrue(completed)
    }

    @Test
    fun `should return a correct DismissedResults on successful response when calling dismissed API`() =
        runBlocking {
            val latch = CountDownLatch(1)

            server.enqueueResponse("dismissed.json", 200)
            server.start()

            val survey = Survey("id", "companyId", "title", null, null, null)
            val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
            iterateApi.dismissed(
                survey,
                object : ApiResponseCallback<DismissedResults> {
                    override fun onSuccess(result: DismissedResults) {
                        val expectedDismissedResults = DismissedResults(
                            id = "60f861a68718c20001c00f80",
                            lastDismissed = "2021-07-21T18:04:22.576376885Z"
                        )
                        assertEquals(expectedDismissedResults, result)
                        latch.countDown()
                    }

                    override fun onError(e: Exception) {
                        throw Exception("This callback should not be called")
                    }
                }
            )

            val completed = latch.await(3, TimeUnit.SECONDS)
            assertTrue(completed)
        }

    @Test
    fun `should return an Exception on failed response when calling dismissed API`() = runBlocking {
        val latch = CountDownLatch(1)

        // Set the HTTP response code to 401
        server.enqueueResponse("dismissed.json", 401)
        server.start()

        val survey = Survey("id", "companyId", "title", null, null, null)
        val iterateApi = DefaultIterateApi("api key", server.url("/").toString())
        iterateApi.dismissed(
            survey,
            object : ApiResponseCallback<DismissedResults> {
                override fun onSuccess(result: DismissedResults) {
                    throw Exception("This callback should not be called")
                }

                override fun onError(e: Exception) {
                    latch.countDown()
                }
            }
        )

        val completed = latch.await(3, TimeUnit.SECONDS)
        assertTrue(completed)
    }
}
