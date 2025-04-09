package com.huanli233.biliwebapi.bean.video

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rights(
    val bp: Int,
    val elec: Int,
    val download: Int,
    val movie: Int,
    val pay: Int,
    val hd5: Int,
    @SerializedName("no_reprint") val noReprint: Int,
    val autoplay: Int,
    @SerializedName("ugc_pay") val ugcPay: Int,
    @SerializedName("is_cooperation") val isCooperation: Int,
    @SerializedName("ugc_pay_preview") val ugcPayPreview: Int,
    @SerializedName("no_background") val noBackground: Int,
    @SerializedName("clean_mode") val cleanMode: Int,
    @SerializedName("is_stein_gate") val isSteinGate: Int,
    @SerializedName("is_360") val is360: Int,
    @SerializedName("no_share") val noShare: Int,
    @SerializedName("arc_pay") val arcPay: Int,
    @SerializedName("free_watch") val freeWatch: Int
) : Parcelable