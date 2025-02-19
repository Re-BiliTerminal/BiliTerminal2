package com.huanli233.biliwebapi.bean.login_info

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.huanli233.biliwebapi.bean.user.*

data class NavInfo(
    @Expose val isLogin: Boolean,
    @SerializedName("email_verified") @Expose val emailVerified: Int,
    @Expose val face: String,
    @SerializedName("level_info") @Expose val levelInfo: LevelInfo,
    @Expose val mid: Long,
    @SerializedName("mobile_verified") @Expose val mobileVerified: Int,
    @Expose val money: Int,
    @Expose val moral: Int,
    @Expose val official: Official,
    @Expose val officialVerify: OfficialVerify,
    @Expose val pendant: Pendant,
    @Expose val scores: Int,
    @Expose val uname: String,
    @Expose val vipDueDate: Long,
    @Expose val vipStatus: Int,
    @Expose val vipType: Int,
    @SerializedName("vip_pay_type") @Expose val vipPayType: Int,
    @SerializedName("vip_theme_type") @Expose val vipThemeType: Int,
    @SerializedName("vip_label") @Expose val vipLabel: VipLabel,
    @SerializedName("vip_avatar_subscript") @Expose val vipAvatarSubscript: Int,
    @SerializedName("vip_nickname_color") @Expose val vipNicknameColor: String,
    @Expose val wallet: Wallet,
    @SerializedName("has_shop") @Expose val hasShop: Boolean,
    @SerializedName("shop_url") @Expose val shopUrl: String,
    @SerializedName("allowance_count") @Expose val allowanceCount: Int,
    @SerializedName("answer_status") @Expose val answerStatus: Int,
    @SerializedName("is_senior_member") @Expose val isSeniorMember: Int,
    @SerializedName("wbi_img") @Expose val wbiImg: WbiImg,
    @SerializedName("is_jury") @Expose val isJury: Boolean
) {
    data class WbiImg(
        @SerializedName("img_url") @Expose val imgUrl: String,
        @SerializedName("sub_url") @Expose val subUrl: String
    )
}
