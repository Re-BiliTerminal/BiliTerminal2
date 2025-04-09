package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VipLabel(
    val path: String,
    val text: String,
    @SerializedName("label_theme") val labelTheme: String,
    @SerializedName("text_color") val textColor: String,
    @SerializedName("bg_style") val bgStyle: Int,
    @SerializedName("bg_color") val bgColor: String
) : Parcelable
