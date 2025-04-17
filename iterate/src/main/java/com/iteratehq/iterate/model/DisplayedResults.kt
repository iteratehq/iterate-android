package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class DisplayedResults(
    val id: String?,
    @SerializedName("last_displayed")
    val lastDisplayed: String?,
)
