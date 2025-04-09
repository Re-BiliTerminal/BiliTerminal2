package com.huanli233.biliwebapi.bean.login

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CookieActivePayload(
    val payload: String
) : Parcelable
