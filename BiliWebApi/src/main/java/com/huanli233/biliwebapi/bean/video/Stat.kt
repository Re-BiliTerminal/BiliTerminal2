package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.dynamic.ChineseNumberAdapter
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stat(
    val aid: Long,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val view: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val danmaku: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val reply: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val favorite: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val coin: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val share: Int,
    
    @SerializedName("now_rank")
    val nowRank: Int,
    
    @SerializedName("his_rank")
    val hisRank: Int,
    
    @JsonAdapter(ChineseNumberAdapter::class)
    val like: Int,
    
    val dislike: Int,
    
    val evaluation: String,
    
    val vt: Int
) : Parcelable
