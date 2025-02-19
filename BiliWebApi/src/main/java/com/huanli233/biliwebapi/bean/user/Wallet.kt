package com.huanli233.biliwebapi.bean.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Wallet(
    @Expose val mid: Long,
    @SerializedName("bcoin_balance") @Expose val bcoinBalance: Int,
    @SerializedName("coupon_balance") @Expose val couponBalance: Int,
    @SerializedName("coupon_due_time") @Expose val couponDueTime: Long
)
