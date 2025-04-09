package com.huanli233.biliwebapi.bean.loginInfo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoinCount(
    val money: Double
) : Parcelable
