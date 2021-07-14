package com.iteratehq.iterate.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Survey(
    val id: String,
    @SerializedName("company_id")
    val companyId: String,
    val title: String,
    val prompt: Prompt?,
    val color: String?
) : Parcelable
