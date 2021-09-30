package com.iteratehq.iterate.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Prompt(
    val message: String,
    @SerializedName("button_text")
    val buttonText: String
) : Parcelable
