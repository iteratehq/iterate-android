package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

data class EmbedContext(
    val app: AppContext?,
    val event: EventContext?,
    val targeting: TargetingContext?,
    val tracking: TrackingContext?,
    val type: EmbedType?,
    @SerializedName("user_traits")
    val userTraits: UserTraitsContext?
)

enum class EmbedType(val value: String) {
    MOBILE("mobile")
}
