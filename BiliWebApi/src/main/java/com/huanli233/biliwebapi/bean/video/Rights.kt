package com.huanli233.biliwebapi.bean.video

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Rights(
    @Expose val bp: Int,
    @Expose val elec: Int,
    @Expose val download: Int,
    @Expose val movie: Int,
    @Expose val pay: Int,
    @Expose val hd5: Int,
    @Expose @SerializedName("no_reprint") val noReprint: Int,
    @Expose val autoplay: Int,
    @Expose @SerializedName("ugc_pay") val ugcPay: Int,
    @Expose @SerializedName("is_cooperation") val isCooperation: Int,
    @Expose @SerializedName("ugc_pay_preview") val ugcPayPreview: Int,
    @Expose @SerializedName("no_background") val noBackground: Int,
    @Expose @SerializedName("clean_mode") val cleanMode: Int,
    @Expose @SerializedName("is_stein_gate") val isSteinGate: Int,
    @Expose @SerializedName("is_360") val is360: Int,
    @Expose @SerializedName("no_share") val noShare: Int,
    @Expose @SerializedName("arc_pay") val arcPay: Int,
    @Expose @SerializedName("free_watch") val freeWatch: Int
)