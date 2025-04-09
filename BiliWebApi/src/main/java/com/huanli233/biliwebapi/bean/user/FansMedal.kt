package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FansMedalInfo(
    val show: Boolean,
    val wear: Boolean,
    val medal: FansMedal
) : Parcelable

@Parcelize
data class FansMedal(
    val uid: Long,
    @SerializedName("target_id") val targetId: Long,
    @SerializedName("medal_id") val medalId: Int,
    val level: Int,
    @SerializedName("medal_name") val medalName: String,
    @SerializedName("medal_color") val medalColor: Int,
    val intimacy: Int,
    @SerializedName("next_intimacy") val nextIntimacy: Int,
    @SerializedName("day_limit") val dayLimit: Int,
    @SerializedName("today_feed") val todayFeed: Int,
    @SerializedName("medal_color_start") val medalColorStart: Int,
    @SerializedName("medal_color_end") val medalColorEnd: Int,
    @SerializedName("medal_color_border") val medalColorBorder: Int,
    @SerializedName("is_lighted") val isLighted: Int,
    @SerializedName("light_status") val lightStatus: Int,
    @SerializedName("wearing_status") val wearingStatus: Int,
    val score: Long
) : Parcelable