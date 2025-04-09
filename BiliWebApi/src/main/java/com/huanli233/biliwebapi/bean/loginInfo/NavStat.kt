package com.huanli233.biliwebapi.bean.loginInfo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavStat(
    val following: Int,
    val follower: Int,
    @SerializedName("dynamic_count") val dynamicCount: Int
) : Parcelable
