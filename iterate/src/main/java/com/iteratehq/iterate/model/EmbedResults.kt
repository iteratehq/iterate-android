package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

internal data class EmbedResults(
    val auth: Auth?,
    val survey: Survey?,
    val triggers: List<Trigger>?,
    val tracking: Tracking
)

internal data class Auth(
    val token: String
)

internal data class Trigger(
    val type: TriggerType,
    val options: TriggerOptions
)

internal enum class TriggerType(val value: String) {
    @SerializedName("exit")
    EXIT("exit"),

    @SerializedName("scroll")
    SCROLL("scroll"),

    @SerializedName("seconds")
    SECONDS("seconds")
}

internal data class TriggerOptions(
    val seconds: Int?
)

internal data class Tracking(
    val lastUpdated: Long
)
