package com.huanli233.biliwebapi.bean.favorite

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.UserInfo
import com.huanli233.biliwebapi.bean.video.Stat
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteVideosResponse(
    val archives: List<FavoriteVideo>?
) : Parcelable

@Parcelize
data class FavoriteVideo(
    val aid: Long,
    val bvid: String,
    val cid: Long,
    val title: String,
    val pic: String,
    val duration: Int,
    val owner: UserInfo,
    val stat: Stat
) : Parcelable
