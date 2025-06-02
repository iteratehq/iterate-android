package com.iteratehq.iterate.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.iteratehq.iterate.model.UserTraits
import java.lang.reflect.Type
import java.util.Date

internal interface IterateSharedPrefs {
    fun clear()

    fun getLastUpdated(): Long?

    fun getUserAuthToken(): String?

    fun getUserTraits(): UserTraits?

    fun setLastUpdated(lastUpdated: Long)

    fun setUserAuthToken(userAuthToken: String)

    fun setUserTraits(userTraits: UserTraits)
}

internal class DefaultIterateSharedPrefs(
    private val context: Context,
    useEncryptedSharedPreferences: Boolean = true,
) : IterateSharedPrefs {
    private val isEncrypted = useEncryptedSharedPreferences && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    private val cryptoManager = CryptoManager(isEncrypted)
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(
            if (isEncrypted) ENCRYPTED_PREFS_FILE else PLAIN_PREFS_FILE,
            Context.MODE_PRIVATE,
        )
    }

    override fun clear() {
        prefs.edit()
            .clear()
            .apply()
    }

    override fun getLastUpdated(): Long? {
        if (!prefs.contains(LAST_UPDATED)) {
            return null
        }
        return if (isEncrypted) {
            cryptoManager.decrypt(prefs.getString(LAST_UPDATED, null))?.toLong()
        } else {
            prefs.getLong(LAST_UPDATED, -1)
        }
    }

    override fun getUserAuthToken(): String? {
        val value = prefs.getString(USER_AUTH_TOKEN, null)
        return if (isEncrypted) cryptoManager.decrypt(value) else value
    }

    override fun getUserTraits(): UserTraits? {
        val stored = prefs.getString(USER_TRAITS, null) ?: return null
        val userTraitsJson = if (isEncrypted) cryptoManager.decrypt(stored) else stored
            ?: return null
        val type = object : TypeToken<UserTraits?>() {}.type
        return Gson().fromJson(userTraitsJson, type)
    }

    override fun setLastUpdated(lastUpdated: Long) {
        val value =
            if (isEncrypted) cryptoManager.encrypt(lastUpdated.toString()) else lastUpdated.toString()
        prefs.edit().apply {
            if (isEncrypted) {
                putString(LAST_UPDATED, value)
            } else {
                putLong(LAST_UPDATED, lastUpdated)
            }
        }.apply()
    }

    override fun setUserAuthToken(userAuthToken: String) {
        val value = if (isEncrypted) cryptoManager.encrypt(userAuthToken) else userAuthToken
        prefs.edit()
            .putString(USER_AUTH_TOKEN, value)
            .apply()
    }

    override fun setUserTraits(userTraits: UserTraits) {
        val gson =
            GsonBuilder()
                .registerTypeAdapter(Date::class.java, DateSerializer())
                .create()

        val userTraitsJson = gson.toJson(userTraits)
        val value = if (isEncrypted) cryptoManager.encrypt(userTraitsJson) else userTraitsJson
        prefs.edit()
            .putString(USER_TRAITS, value)
            .apply()
    }

    private companion object {
        private const val ENCRYPTED_PREFS_FILE = "EncryptedIterateSharedPrefs"
        private const val PLAIN_PREFS_FILE = "PlainIterateSharedPrefs"
        private const val LAST_UPDATED = "LAST_UPDATED"
        private const val USER_AUTH_TOKEN = "USER_AUTH_TOKEN"
        private const val USER_TRAITS = "USER_TRAITS"
    }
}

internal class DateSerializer : JsonSerializer<Date> {
    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return if (src == null) {
            JsonNull.INSTANCE
        } else {
            JsonObject().apply {
                addProperty("type", "date")
                addProperty("value", src.getTime() / 1000)
            }
        }
    }
}
