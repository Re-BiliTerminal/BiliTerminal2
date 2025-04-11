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
) : Parcelable {
    fun toUserInfo() = card.toUserInfo().copy(
        follower = follower,
        isFollowed = following
    )
}

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
) : Parcelable {
    fun toUserInfo() = UserInfo(
        mid = mid.toLongOrNull() ?: -1,
        title = null,
        name = name,
        face = face,
        vip = vip,
        official = official,
        follower = fans,
        sex = sex,
        sign = sign,
        rank = -1,
        level = levelInfo.currentLevel,
        silence = 0,
        coins = -1,
        fansBadge = false,
        fansMedal = null,
        pendant = pendant,
        nameplate = nameplate,
        isFollowed = false,
        topPhoto = "",
        systemNotice = null,
        liveRoom = null,
        series = SeriesStatus(userUpgradeStatus = -1, showUpgradeWindow = false),
        isSeniorMember = 1,
        contract = ContractStatus(
            isDisplay = false,
            isFollowDisplay = false
        ),
        school = School("")
    )
}

@Parcelize
data class SpaceImage(
    @SerializedName("s_img") val sImg: String,
    @SerializedName("l_img") val lImg: String,
) : Parcelable