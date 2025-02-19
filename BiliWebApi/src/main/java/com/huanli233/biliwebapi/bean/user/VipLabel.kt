package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class VipLabel(
    @Expose val path: String,
    @Expose val text: String,
    @SerializedName("label_theme") @Expose val labelTheme: String
)
