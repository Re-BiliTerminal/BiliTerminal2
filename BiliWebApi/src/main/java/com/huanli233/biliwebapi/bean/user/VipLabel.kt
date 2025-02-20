package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VipLabel(
    @Expose val path: String,
    @Expose val text: String,
    @Expose @SerializedName("label_theme") val labelTheme: String,
    @Expose @SerializedName("text_color") val textColor: String,
    @Expose @SerializedName("bg_style") val bgStyle: Int,
    @Expose @SerializedName("bg_color") val bgColor: String
)
