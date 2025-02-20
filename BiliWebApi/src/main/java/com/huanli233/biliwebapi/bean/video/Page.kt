package com.huanli233.biliwebapi.bean.video

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Page(
    @Expose
    val cid: Long,

    @Expose
    val page: Int,

    @Expose
    val from: String,

    @Expose
    val part: String,

    @Expose
    val duration: Int,

    @Expose
    val vid: String,

    @Expose
    val weblink: String,

    @Expose
    val dimension: Dimension,

    @Expose
    @SerializedName("first_frame")
    val firstFrame: String
)

data class Dimension(
    @Expose
    val width: Int,

    @Expose
    val height: Int,

    @Expose
    val rotate: Int
)