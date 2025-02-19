package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LevelInfo(
    @SerializedName("current_level") @Expose val currentLevel: Int,
    @SerializedName("current_min") @Expose val currentMin: Int,
    @SerializedName("current_exp") @Expose val currentExp: Int,
    @SerializedName("next_exp") @Expose val nextExp: String
)
