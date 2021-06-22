package com.iteratehq.iterate.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.model.UserTraits

internal interface IterateSharedPrefs {
    fun getLastUpdated(): Long?
    fun getUserTraits(): MutableMap<String, Any>?
    fun setLastUpdated(lastUpdated: Long)
    fun setUserTraits(userTraits: UserTraits)
}

internal class IterateSharedPrefsImpl(
    private val context: Context
) : IterateSharedPrefs {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    override fun getLastUpdated(): Long? {
        return if (prefs.contains(LAST_UPDATED)) {
            prefs.getLong(LAST_UPDATED, -1)
        } else {
            null
        }
    }

    override fun getUserTraits(): MutableMap<String, Any>? {
        val userTraitsJson = prefs.getString(USER_TRAITS, "")
        val type = object : TypeToken<MutableMap<String, Any>?>() {}.type
        return Gson().fromJson(userTraitsJson, type)
    }

    override fun setLastUpdated(lastUpdated: Long) {
        prefs.edit()
            .putLong(LAST_UPDATED, lastUpdated)
            .apply()
    }

    override fun setUserTraits(userTraits: UserTraits) {
        val userTraitsJson = Gson().toJson(userTraits)
        prefs.edit()
            .putString(USER_TRAITS, userTraitsJson)
            .apply()
    }

    private companion object {
        private const val PREF_FILE = "IterateSharedPrefs"
        private const val LAST_UPDATED = "LAST_UPDATED"
        private const val USER_TRAITS = "USER_TRAITS"
    }
}
