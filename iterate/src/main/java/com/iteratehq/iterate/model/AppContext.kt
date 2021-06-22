package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

data class AppContext(
    @SerializedName("url_scheme")
    val urlScheme: String?,
    val version: String?
)
