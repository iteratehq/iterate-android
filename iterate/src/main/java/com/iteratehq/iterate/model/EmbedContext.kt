package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EmbedContext(
    val app: AppContext?,
    val event: EventContext?,
    val targeting: TargetingContext?,
    val tracking: TrackingContext?,
    val type: EmbedType?,
    @SerializedName("user_traits")
    val userTraits: UserTraitsContext?
)

@Keep
enum class EmbedType(val value: String) {
    @SerializedName("mobile")
    MOBILE("mobile")
}
