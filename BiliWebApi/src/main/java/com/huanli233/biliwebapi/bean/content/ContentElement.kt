package com.huanli233.biliwebapi.bean.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentElement(
    @SerializedName("raw_text") val rawText: String,
    val type: Int,
    @SerializedName("biz_id") val bizId: Int
) : Parcelable
