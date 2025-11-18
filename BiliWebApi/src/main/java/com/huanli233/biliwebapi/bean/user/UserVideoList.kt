package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserVideoListResponse(
    val list: UserVideoList
) : Parcelable

@Parcelize
data class UserVideoList(
    val vlist: List<VideoInfo>
) : Parcelable
