package com.huanli233.biliwebapi.bean.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

const val BIZ_ID_TEXT = 1L
const val BIZ_ID_MENTION = 2L
const val BIZ_ID_EMOJI = 9L

@Parcelize
data class ContentElement(
    @SerializedName("raw_text") val rawText: String,
    val type: Int,
    @SerializedName("biz_id") val bizId: Long
) : Parcelable