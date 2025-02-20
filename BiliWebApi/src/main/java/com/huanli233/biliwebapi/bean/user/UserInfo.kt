package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserInfo(
    @Expose val mid: Long,
    @Expose val title: String?,
    @Expose val name: String,
    @Expose val face: String,
    @Expose val vip: Vip?,
    @Expose val official: Official,
    @Expose val follower: Int
)

data class Vip(
    @Expose val type: Int,
    @Expose val status: Int,
    @Expose @SerializedName("due_date") val dueDate: Long,
    @Expose @SerializedName("vip_pay_type") val vipPayType: Int,
    @Expose @SerializedName("theme_type") val themeType: Int,
    @Expose val label: VipLabel,
    @Expose @SerializedName("avatar_subscript") val avatarSubscript: Int,
    @Expose @SerializedName("nickname_color") val nicknameColor: String,
    @Expose val role: Int
)