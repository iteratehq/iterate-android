package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

internal data class DisplayedResults(
    val id: String?,
    @SerializedName("last_displayed")
    val lastDisplayed: String?
)
