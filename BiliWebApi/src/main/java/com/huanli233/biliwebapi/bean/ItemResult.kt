package com.huanli233.biliwebapi.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemResult<T : Parcelable>(
    val item: T,
    val fallback: Fallback? = null
) : Parcelable

@Parcelize
data class Fallback(
    val id: String? = null,
    val type: Int = 0
) : Parcelable
