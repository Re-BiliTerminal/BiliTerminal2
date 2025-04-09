package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.LevelInfo
import com.huanli233.biliwebapi.bean.user.Vip
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerInfo(
    val aid: Long,
    val bvid: String,
    @SerializedName("allow_bp")
    val allowBp: Boolean,
    @SerializedName("no_share")
    val noShare: Boolean,
    val cid: Long,
    @SerializedName("max_limit")
    val maxLimit: Int,
    @SerializedName("page_no")
    val pageNo: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("ip_info")
    val ipInfo: IpInfo,
    @SerializedName("login_mid")
    val loginMid: Long,
    @SerializedName("login_mid_hash")
    val loginMidHash: String,
    @SerializedName("is_owner")
    val isOwner: Boolean,
    val name: String,
    val permission: String,
    @SerializedName("level_info")
    val levelInfo: LevelInfo,
    val vip: Vip,
    @SerializedName("answer_status")
    val answerStatus: Int,
    @SerializedName("block_time")
    val blockTime: Long,
    val role: String,
    @SerializedName("last_play_time")
    val lastPlayTime: Long,
    @SerializedName("last_play_cid")
    val lastPlayCid: Long,
    @SerializedName("now_time")
    val nowTime: Long,
    @SerializedName("online_count")
    val onlineCount: Int,
    @SerializedName("need_login_subtitle")
    val needLoginSubtitle: Boolean,
    val subtitle: SubtitleInfo,
    @SerializedName("player_icon")
    val playerIcon: PlayerIcon,
    @SerializedName("preview_toast")
    val previewToast: String,
    val options: PlayerOptions,
    @SerializedName("guide_attention")
    val guideAttention: List<GuideAttention>,
    @SerializedName("online_switch")
    val onlineSwitch: OnlineSwitch,
    val fawkes: Fawkes,
    @SerializedName("show_switch")
    val showSwitch: ShowSwitch,
    @SerializedName("bgm_info")
    val bgmInfo: BgmInfo,
    @SerializedName("toast_block")
    val toastBlock: Boolean,
    @SerializedName("is_upower_exclusive")
    val isUpowerExclusive: Boolean,
    @SerializedName("is_upower_play")
    val isUpowerPlay: Boolean,
    @SerializedName("is_ugc_pay_preview")
    val isUgcPayPreview: Boolean,
    @SerializedName("elec_high_level")
    val elecHighLevel: ElecHighLevel,
    @SerializedName("disable_show_up_info")
    val disableShowUpInfo: Boolean
) : Parcelable

@Parcelize
data class IpInfo(
    val ip: String,
    @SerializedName("zone_ip")
    val zoneIp: String,
    @SerializedName("zone_id")
    val zoneId: Long,
    val country: String,
    val province: String,
    val city: String
) : Parcelable

@Parcelize
data class PlayerIcon(
    val ctime: Long
) : Parcelable

@Parcelize
data class PlayerOptions(
    @SerializedName("is_360")
    val is360: Boolean,
    @SerializedName("without_vip")
    val withoutVip: Boolean
) : Parcelable

@Parcelize
data class GuideAttention(
    val type: Int,
    val from: Int,
    val to: Int,
    @SerializedName("pos_x")
    val posX: Double,
    @SerializedName("pos_y")
    val posY: Double
) : Parcelable

@Parcelize
data class OnlineSwitch(
    @SerializedName("enable_gray_dash_playback")
    val enableGrayDashPlayback: String,
    @SerializedName("new_broadcast")
    val newBroadcast: String,
    @SerializedName("realtime_dm")
    val realtimeDm: String,
    @SerializedName("subtitle_submit_switch")
    val subtitleSubmitSwitch: String
) : Parcelable

@Parcelize
data class Fawkes(
    @SerializedName("config_version")
    val configVersion: Long,
    @SerializedName("ff_version")
    val ffVersion: Long
) : Parcelable

@Parcelize
data class ShowSwitch(
    @SerializedName("long_progress")
    val longProgress: Boolean
) : Parcelable

@Parcelize
data class BgmInfo(
    @SerializedName("music_id")
    val musicId: String,
    @SerializedName("music_title")
    val musicTitle: String,
    @SerializedName("jump_url")
    val jumpUrl: String
) : Parcelable

@Parcelize
data class ElecHighLevel(
    @SerializedName("privilege_type")
    val privilegeType: Int,
    val title: String,
    @SerializedName("sub_title")
    val subTitle: String,
    @SerializedName("show_button")
    val showButton: Boolean,
    @SerializedName("button_text")
    val buttonText: String,
    @SerializedName("jump_url")
    val jumpUrl: String,
    val intro: String,
    val new: Boolean
) : Parcelable
