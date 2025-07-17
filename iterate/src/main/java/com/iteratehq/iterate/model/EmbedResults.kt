package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class EmbedResults(
    val auth: Auth?,
    val survey: Survey?,
    val triggers: List<Trigger>?,
    val tracking: Tracking?,
    @SerializedName("event_traits")
    val eventTraits: UserTraits?,
)

@Keep
internal data class Auth(
    val token: String,
)

@Keep
internal data class Trigger(
    val type: TriggerType,
    val options: TriggerOptions,
)

@Keep
internal enum class TriggerType(
    val value: String,
) {
    @SerializedName("exit")
    EXIT("exit"),

    @SerializedName("scroll")
    SCROLL("scroll"),

    @SerializedName("seconds")
    SECONDS("seconds"),
}

@Keep
internal data class TriggerOptions(
    val seconds: Int?,
)

@Keep
internal data class Tracking(
    @SerializedName("last_updated")
    val lastUpdated: Long,
)
