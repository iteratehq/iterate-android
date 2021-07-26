package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

internal data class DismissedResults(
    val id: String?,
    @SerializedName("last_dismissed")
    val lastDismissed: String?
)
