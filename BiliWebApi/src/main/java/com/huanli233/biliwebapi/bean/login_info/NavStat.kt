package com.huanli233.biliwebapi.bean.login_info

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NavStat(
    @Expose val following: Int,
    @Expose val follower: Int,
    @SerializedName("dynamic_count") @Expose val dynamicCount: Int
)
