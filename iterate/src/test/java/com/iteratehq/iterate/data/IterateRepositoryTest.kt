package com.iteratehq.iterate.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iteratehq.iterate.data.local.IterateInMemoryStore
import com.iteratehq.iterate.data.local.IterateSharedPrefs
import com.iteratehq.iterate.data.remote.IterateApi
import com.iteratehq.iterate.model.EventTraits
import com.iteratehq.iterate.model.UserTraits
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class IterateRepositoryTest {

    private lateinit var repository: IterateRepository

    @Mock
    private lateinit var iterateApi: IterateApi

    @Mock
    private lateinit var iterateInMemoryStore: IterateInMemoryStore

    @Mock
    private lateinit var iterateSharedPrefs: IterateSharedPrefs

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)

        val context: Context = ApplicationProvider.getApplicationContext()
        repository = DefaultIterateRepository(
            context,
            "API_KEY",
            true,
            iterateApi,
            iterateInMemoryStore,
            iterateSharedPrefs
        )
    }

    @Test
    fun `should correctly store companyAuthToken`() {
        doAnswer { invocation ->
            val data = invocation.arguments[0] as String
            `when`(iterateInMemoryStore.getCompanyAuthToken()).thenReturn(data)
        }.`when`(iterateInMemoryStore).setCompanyAuthToken(anyString())

        val companyAuthToken = "companyAuthToken"
        repository.setCompanyAuthToken(companyAuthToken)
        val outputCompanyAuthToken = repository.getCompanyAuthToken()

        assertEquals(companyAuthToken, outputCompanyAuthToken)
        verify(iterateInMemoryStore).setCompanyAuthToken(anyString())
        verify(iterateInMemoryStore).getCompanyAuthToken()
    }

    @Test
    fun `should correctly store eventTraits`() {
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val data = invocation.arguments[0] as Map<Long, EventTraits>
            `when`(iterateInMemoryStore.getEventTraitsMap()).thenReturn(data)
        }.`when`(iterateInMemoryStore).setEventTraitsMap(anyMap())

        val eventTraits = EventTraits("key" to "value")
        val responseId = 1000L
        repository.setEventTraits(eventTraits, responseId)
        val outputEventTraits = repository.getEventTraits(responseId)

        assertEquals(eventTraits, outputEventTraits)
        verify(iterateInMemoryStore).setEventTraitsMap(anyMap())
        verify(iterateInMemoryStore).getEventTraitsMap()
    }

    @Test
    fun `should correctly store lastUpdated`() {
        doAnswer { invocation ->
            val data = invocation.arguments[0] as Long
            `when`(iterateSharedPrefs.getLastUpdated()).thenReturn(data)
        }.`when`(iterateSharedPrefs).setLastUpdated(anyLong())

        val lastUpdated = 1000L
        repository.setLastUpdated(lastUpdated)
        val outputLastUpdated = repository.getLastUpdated()

        assertEquals(lastUpdated, outputLastUpdated)
        verify(iterateSharedPrefs).setLastUpdated(anyLong())
        verify(iterateSharedPrefs).getLastUpdated()
    }

    @Test
    fun `should correctly store previewSurveyId`() {
        doAnswer { invocation ->
            val data = invocation.arguments[0] as String
            `when`(iterateInMemoryStore.getPreviewSurveyId()).thenReturn(data)
        }.`when`(iterateInMemoryStore).setPreviewSurveyId(anyString())

        val previewSurveyId = "previewSurveyId"
        repository.setPreviewSurveyId(previewSurveyId)
        val outputPreviewSurveyId = repository.getPreviewSurveyId()

        assertEquals(previewSurveyId, outputPreviewSurveyId)
        verify(iterateInMemoryStore).setPreviewSurveyId(anyString())
        verify(iterateInMemoryStore).getPreviewSurveyId()
    }

    @Test
    fun `should correctly store userAuthToken`() {
        doAnswer { invocation ->
            val data = invocation.arguments[0] as String
            `when`(iterateSharedPrefs.getUserAuthToken()).thenReturn(data)
        }.`when`(iterateSharedPrefs).setUserAuthToken(anyString())

        val userAuthToken = "userAuthToken"
        repository.setUserAuthToken(userAuthToken)
        val outputUserAuthToken = repository.getUserAuthToken()

        assertEquals(userAuthToken, outputUserAuthToken)
        verify(iterateSharedPrefs).setUserAuthToken(anyString())
        verify(iterateSharedPrefs).getUserAuthToken()
    }

    @Test
    fun `should correctly store userTraits`() {
        val userTraits = UserTraits("key" to "value")

        doAnswer { invocation ->
            val data = invocation.arguments[0] as UserTraits
            `when`(iterateSharedPrefs.getUserTraits()).thenReturn(data)
        }.`when`(iterateSharedPrefs).setUserTraits(userTraits)

        repository.setUserTraits(userTraits)
        val outputUserTraits = repository.getUserTraits()

        assertEquals(userTraits, outputUserTraits)
        verify(iterateSharedPrefs).setUserTraits(userTraits)
        verify(iterateSharedPrefs).getUserTraits()
    }
}
