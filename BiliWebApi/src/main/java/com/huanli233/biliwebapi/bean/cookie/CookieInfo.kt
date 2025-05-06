package com.huanli233.biliwebapi.bean.cookie

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CookieInfo(
    val refresh: Boolean,
    val timestamp: Long
): Parcelable
