package com.huanli233.biliwebapi.bean.video

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Stat(
    @Expose
    val aid: Long,
    @Expose
    val view: Int,
    @Expose
    val danmaku: Int,
    @Expose
    val reply: Int,
    @Expose
    val favorite: Int,
    @Expose
    val coin: Int,
    @Expose
    val share: Int,
    @Expose
    @SerializedName("now_rank")
    val nowRank: Int,
    @Expose
    @SerializedName("his_rank")
    val hisRank: Int,
    @Expose
    val like: Int,
    @Expose
    val dislike: Int,
    @Expose
    val evaluation: String, //评分
    @Expose
    val vt: Int
)
