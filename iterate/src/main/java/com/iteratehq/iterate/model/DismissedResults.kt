package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class DismissedResults(
    val id: String?,
    @SerializedName("last_dismissed")
    val lastDismissed: String?,
)
