package com.iteratehq.iterate.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AppContext(
    @SerializedName("url_scheme")
    val urlScheme: String?,
    val version: String?,
)
