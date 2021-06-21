package com.iteratehq.iterate.model

import com.google.gson.annotations.SerializedName

data class Survey(
    val id: String,
    @SerializedName("company_id")
    val companyId: String,
    val title: String,
    val prompt: Prompt?,
    val color: String?
)
