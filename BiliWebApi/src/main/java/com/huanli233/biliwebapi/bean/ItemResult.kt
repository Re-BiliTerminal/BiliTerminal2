package com.huanli233.biliwebapi.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ItemResult<T : Any>(
    val item: T,
    val fallback: Fallback? = null
)

@Parcelize
data class Fallback(
    val id: String? = null,
    val type: Int = 0
) : Parcelable
