package com.huanli233.biliwebapi.bean.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryResponse(
    val cursor: HistoryCursor,
    val list: List<HistoryItem>
) : Parcelable

@Parcelize
data class HistoryCursor(
    val max: Long,
    @SerializedName("view_at") val viewAt: Long,
    val business: String
) : Parcelable

@Parcelize
data class HistoryItem(
    val title: String,
    val cover: String,
    @SerializedName("author_name") val authorName: String,
    val progress: Int,
    val duration: Int,
    val history: HistoryDetail,
    @SerializedName("view_at") val viewAt: Long,
    @SerializedName("view_count") val viewCount: Int? = null
) : Parcelable

@Parcelize
data class HistoryDetail(
    val oid: Long,
    val bvid: String,
    val cid: Long
) : Parcelable
