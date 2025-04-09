package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pendant(
    val pid: Int,
    val name: String,
    val image: String,
    val expire: Int
) : Parcelable
