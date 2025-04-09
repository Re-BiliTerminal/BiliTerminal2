package com.huanli233.biliwebapi.bean.loginInfo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.LevelInfo
import com.huanli233.biliwebapi.bean.user.Official
import com.huanli233.biliwebapi.bean.user.OfficialVerify
import com.huanli233.biliwebapi.bean.user.Pendant
import com.huanli233.biliwebapi.bean.user.VipLabel
import com.huanli233.biliwebapi.bean.user.Wallet
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavInfo(
    val isLogin: Boolean,
    @SerializedName("email_verified") val emailVerified: Int,
    val face: String,
    @SerializedName("level_info") val levelInfo: LevelInfo,
    val mid: Long,
    @SerializedName("mobile_verified") val mobileVerified: Int,
    val money: Double,
    val moral: Int,
    val official: Official,
    val officialVerify: OfficialVerify,
    val pendant: Pendant,
    val scores: Int,
    val uname: String,
    val vipDueDate: Long,
    val vipStatus: Int,
    val vipType: Int,
    @SerializedName("vip_pay_type") val vipPayType: Int,
    @SerializedName("vip_theme_type") val vipThemeType: Int,
    @SerializedName("vip_label") val vipLabel: VipLabel,
    @SerializedName("vip_avatar_subscript") val vipAvatarSubscript: Int,
    @SerializedName("vip_nickname_color") val vipNicknameColor: String,
    val wallet: Wallet,
    @SerializedName("has_shop") val hasShop: Boolean,
    @SerializedName("shop_url") val shopUrl: String,
    @SerializedName("allowance_count") val allowanceCount: Int,
    @SerializedName("answer_status") val answerStatus: Int,
    @SerializedName("is_senior_member") val isSeniorMember: Int,
    @SerializedName("wbi_img") val wbiImg: WbiImg,
    @SerializedName("is_jury") val isJury: Boolean
) : Parcelable {
    @Parcelize
    data class WbiImg(
        @SerializedName("img_url") val imgUrl: String,
        @SerializedName("sub_url") val subUrl: String
    ) : Parcelable
}
