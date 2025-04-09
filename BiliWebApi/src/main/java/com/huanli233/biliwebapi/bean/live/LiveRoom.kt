package com.huanli233.biliwebapi.bean.live

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LiveRoom(
    val roomStatus: Int,
    val liveStatus: Int,
    val url: String,
    val title: String,
    val cover: String,
    @SerializedName("watched_show") val watchedShow: WatchedShow,
    @SerializedName("roomid") val roomId: Long,
    val roundStatus: Int,
    @SerializedName("broadcast_type") val broadcastType: Int,
    val uid: Long,
    val uname: String,
    val tags: String,
    val description: String,
    val online: Int,
    @SerializedName("user_cover") val userCover: String,
    @SerializedName("user_cover_flag") val userCoverFlag: Int,
    @SerializedName("system_cover") val systemCover: String,
    val keyframe: String,
    @SerializedName("show_cover") val showCover: String,
    val face: String,
    @SerializedName("area_parent_id") val areaParentId: Int,
    @SerializedName("area_parent_name") val areaParentName: String,
    @SerializedName("area_id") val areaId: Int,
    @SerializedName("area_name") val areaName: String,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("group_id") val groupId: Long,
    @SerializedName("live_time") val liveTime: Long,
    val verify: Verify
) : Parcelable {
    @Parcelize
    data class Verify(
        val role: Int,
        val desc: String,
        val type: Int
    ) : Parcelable
}

@Parcelize
data class WatchedShow(
    val switch: Boolean,
    val num: Int,
    @SerializedName("text_small") val textSmall: String,
    @SerializedName("text_large") val textLarge: String,
    val icon: String,
    @SerializedName("icon_location") val iconLocation: String,
    @SerializedName("icon_web") val iconWeb: String
) : Parcelable

