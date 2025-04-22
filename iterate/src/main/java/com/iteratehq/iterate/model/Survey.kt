package com.iteratehq.iterate.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Survey(
    val id: String,
    @SerializedName("company_id")
    val companyId: String,
    val title: String?,
    val prompt: Prompt?,
    val color: String?,
    @SerializedName("color_dark")
    val colorDark: String?,
    @SerializedName("border_radius")
    val borderRadius: String? = null,
    @SerializedName("primary_language")
    val primaryLanguage: String? = null,
    val translations: List<Translation>? = null,
    val appearance: String? = null,
) : Parcelable
