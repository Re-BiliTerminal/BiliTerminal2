package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfficialVerify(
    val type: Int,
    val desc: String
) : Parcelable
