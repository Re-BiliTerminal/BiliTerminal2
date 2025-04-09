package com.huanli233.biliwebapi.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemResult<T : Parcelable>(
    val item: T
) : Parcelable
