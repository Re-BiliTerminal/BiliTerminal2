package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Page(
    val cid: Long,
    val page: Int,
    val from: String,
    val part: String,
    val duration: Int,
    val vid: String,
    val weblink: String,
    val dimension: Dimension,
    @SerializedName("first_frame")
    val firstFrame: String
) : Parcelable

@Parcelize
data class Dimension(
    val width: Int,
    val height: Int,
    val rotate: Int
) : Parcelable