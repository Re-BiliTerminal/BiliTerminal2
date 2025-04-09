package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Official(
    val role: Int,
    val title: String,
    val desc: String,
    val type: Int
) : Parcelable
