package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

data class TrackingContext(
    @SerializedName("last_updated")
    val lastUpdated: Long?
)
