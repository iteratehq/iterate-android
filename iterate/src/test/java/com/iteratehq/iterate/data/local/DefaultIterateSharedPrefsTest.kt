package com.iteratehq.iterate.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iteratehq.iterate.model.UserTraits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DefaultIterateSharedPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val lastUpdated = 100L
    private val userAuthToken = "userAuthToken"
    private val userTraits = UserTraits("key" to "value")

    @Test
    fun `should correctly store lastUpdated`() {
        val prefs = DefaultIterateSharedPrefs(context)
        prefs.setLastUpdated(lastUpdated)
        val outputLastUpdated = prefs.getLastUpdated()

        assertEquals(lastUpdated, outputLastUpdated)
    }

    @Test
    fun `should correctly store userAuthToken`() {
        val prefs = DefaultIterateSharedPrefs(context)
        prefs.setUserAuthToken(userAuthToken)
        val outputUserAuthToken = prefs.getUserAuthToken()

        assertEquals(userAuthToken, outputUserAuthToken)
    }

    @Test
    fun `should correctly store userTraits`() {
        val prefs = DefaultIterateSharedPrefs(context)
        prefs.setUserTraits(userTraits)
        val outputUserTraits = prefs.getUserTraits()

        assertEquals(userTraits, outputUserTraits)
    }

    @Test
    fun `should have all data set to null after being cleared`() {
        val prefs = DefaultIterateSharedPrefs(context).apply {
            setLastUpdated(lastUpdated)
            setUserAuthToken(userAuthToken)
            setUserTraits(userTraits)
        }
        prefs.clear()

        val outputLastUpdated = prefs.getLastUpdated()
        val outputUserAuthToken = prefs.getUserAuthToken()
        val outputUserTraits = prefs.getUserTraits()

        assertNull(outputLastUpdated)
        assertNull(outputUserAuthToken)
        assertNull(outputUserTraits)
    }
}
