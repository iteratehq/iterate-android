package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TrackingContext(
    @SerializedName("last_updated")
    val lastUpdated: Long?,
)
