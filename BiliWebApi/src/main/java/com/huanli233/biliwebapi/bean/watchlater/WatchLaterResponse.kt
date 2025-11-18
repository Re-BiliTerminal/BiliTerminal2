package com.huanli233.biliwebapi.bean.watchlater

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.Stat
import kotlinx.parcelize.Parcelize

@Parcelize
data class WatchLaterResponse(
    val count: Int,
    val list: List<WatchLaterItem>?
) : Parcelable

@Parcelize
data class WatchLaterItem(
    val aid: Long,
    val bvid: String,
    val cid: Long,
    val title: String,
    val pic: String,
    val duration: Int,
    val owner: UserInfo,
    val stat: Stat,
    @SerializedName("add_at") val addAt: Long,
    val progress: Int = 0,
    val state: Int = 0
) : Parcelable
