package com.huanli233.biliwebapi.bean.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wallet(
    val mid: Long,
    @SerializedName("bcoin_balance") val bcoinBalance: Int,
    @SerializedName("coupon_balance") val couponBalance: Int,
    @SerializedName("coupon_due_time") val couponDueTime: Long
) : Parcelable
