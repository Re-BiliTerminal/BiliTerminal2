package com.huanli233.biliwebapi.bean.favorite

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpusFavoriteResponse(
    @SerializedName("has_more") val hasMore: Boolean,
    val items: List<OpusFavoriteItem>?
) : Parcelable

@Parcelize
data class OpusFavoriteItem(
    @SerializedName("opus_id") val opusId: String,
    val title: String?,
    val content: String?,
    val cover: String?,
    @SerializedName("time_text") val timeText: String?
) : Parcelable
