package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserCardInfo(
    val card: UserCard,
    val following: Boolean,
    @SerializedName("archive_count") val archiveCount: Int,
    @SerializedName("article_count") val articleCount: Int,
    val follower: Int,
    @SerializedName("like_num") val likeNum: Int
) : Parcelable

@Parcelize
data class UserCard(
    val mid: String,
    val approve: Boolean,
    val name: String,
    val sex: String,
    val face: String,
    @SerializedName("spacesta") val spaceStatus: Int,
    val fans: Int,
    val friend: Int,
    val attention: Int,
    val sign: String,
    @SerializedName("level_info") val levelInfo: LevelInfo,
    val pendant: Pendant,
    val nameplate: Nameplate,
    @SerializedName("Official") val official: Official,
    @SerializedName("official_verify") val officialVerify: OfficialVerify,
    val vip: Vip,
    val space: SpaceImage
) : Parcelable

@Parcelize
data class SpaceImage(
    @SerializedName("s_img") val sImg: String,
    @SerializedName("l_img") val lImg: String,
) : Parcelable