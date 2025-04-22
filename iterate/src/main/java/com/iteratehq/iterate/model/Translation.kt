package com.iteratehq.iterate.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Translation(
    val language: String,
    val items: Map<String, TranslationItem>?,
) : Parcelable

@Keep
@Parcelize
data class TranslationItem(
    val text: String?,
) : Parcelable
