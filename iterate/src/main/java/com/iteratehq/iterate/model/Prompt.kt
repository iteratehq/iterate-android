package com.iteratehq.iterate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Prompt(
    val message: String,
    val buttonText: String
) : Parcelable
