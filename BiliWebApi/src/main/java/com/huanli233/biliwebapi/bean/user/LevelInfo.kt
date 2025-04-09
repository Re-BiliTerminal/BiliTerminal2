package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LevelInfo(
    @SerializedName("current_level") val currentLevel: Int,
    @SerializedName("current_min") val currentMin: Int,
    @SerializedName("current_exp") val currentExp: Int,
    @SerializedName("next_exp") val nextExp: String
) : Parcelable
