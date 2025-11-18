package com.huanli233.biliwebapi.bean.follow

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FollowListResponse(
    val list: List<FollowUser>?
) : Parcelable

@Parcelize
data class FollowUser(
    val mid: Long,
    val uname: String,
    val face: String,
    val sign: String,
    val mtime: Long = 0
) : Parcelable
