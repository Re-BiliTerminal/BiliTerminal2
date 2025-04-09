package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserId(
    val mid: Long,
    val face: String? = null,
    val name: String? = null,
) : Parcelable
