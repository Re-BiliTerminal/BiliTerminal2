package com.huanli233.biliwebapi.bean.opus

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpusCard(
    @LowerCaseUnderScore val opusId: String,
    val title: String,
    val content: String,
    val cover: String,
    @LowerCaseUnderScore val timeText: String,
    @LowerCaseUnderScore val isExpired: Boolean
) : Parcelable

@Parcelize
data class OpusCardList(
    val items: List<OpusCard>,
    @LowerCaseUnderScore val hasMore: Boolean,
    val total: Int
) : Parcelable