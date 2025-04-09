package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SystemNotice(
    val id: Int,
    val content: String,
    val url: String,
    @SerializedName("notice_type") val noticeType: Int,
    val icon: String,
    @SerializedName("text_color") val textColor: String,
    @SerializedName("bg_color") val bgColor: String
) : Parcelable