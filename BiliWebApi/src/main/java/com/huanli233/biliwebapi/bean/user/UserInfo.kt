package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.live.LiveRoom
import com.huanli233.biliwebapi.util.LowerCaseUnderScore
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val mid: Long,
    val title: String?,
    val name: String,
    val face: String,
    val vip: Vip,
    val official: Official,
    @SerializedName("follower", alternate = ["following"]) val follower: Int,
    val sex: String,
    val sign: String,
    val rank: Int,
    val level: Int,
    val silence: Int,
    val coins: Int,
    @SerializedName("fans_badge") val fansBadge: Boolean,
    @SerializedName("fans_medal") val fansMedal: FansMedalInfo,
    val pendant: Pendant,
    val nameplate: Nameplate,
    @SerializedName("is_followed") val isFollowed: Boolean,
    @SerializedName("top_photo") val topPhoto: String,
    @SerializedName("sys_notice") val systemNotice: SystemNotice,
    val liveRoom: LiveRoom,
    val series: SeriesStatus,
    @SerializedName("is_senior_member") val isSeniorMember: Int,
    val contract: ContractStatus,
    val school: School,
    @SerializedName("email_status") val emailStatus: Int = 0,
    @SerializedName("tel_status") val telephoneStatus: Int = 0,
    @SerializedName("is_contractor") val isContractor: Boolean = false,
    @SerializedName("contract_desc") val contractDesc: String? = null,
    @LowerCaseUnderScore val pubTime: String? = null,
    @LowerCaseUnderScore val pubTs: Long? = null,
) : Parcelable

@Parcelize
data class Vip(
    val type: Int,
    val status: Int,
    @SerializedName("due_date") val dueDate: Long,
    @SerializedName("vip_pay_type") val vipPayType: Int,
    @SerializedName("theme_type") val themeType: Int,
    val label: VipLabel,
    @SerializedName("avatar_subscript") val avatarSubscript: Int,
    @SerializedName("nickname_color") val nicknameColor: String,
    val role: Int
) : Parcelable

@Parcelize
data class School(
    val name: String
) : Parcelable

@Parcelize
data class SeriesStatus(
    @SerializedName("user_upgrade_status") val userUpgradeStatus: Int,
    @SerializedName("show_upgrade_window") val showUpgradeWindow: Boolean
) : Parcelable

@Parcelize
data class ContractStatus(
    @SerializedName("is_display") val isDisplay: Boolean,
    @SerializedName("is_follow_display") val isFollowDisplay: Boolean
) : Parcelable