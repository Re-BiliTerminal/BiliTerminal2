package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Stat(
    val aid: Long,
    
    val view: Int,
    
    val danmaku: Int,
    
    val reply: Int,
    
    val favorite: Int,
    
    val coin: Int,
    
    val share: Int,
    
    @SerializedName("now_rank")
    val nowRank: Int,
    
    @SerializedName("his_rank")
    val hisRank: Int,
    
    val like: Int,
    
    val dislike: Int,
    
    val evaluation: String,
    
    val vt: Int
) : Parcelable
