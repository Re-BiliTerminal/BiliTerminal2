package com.huanli233.biliwebapi.bean.reply

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReplySendResult(
    val reply: Reply? = null
) : Parcelable
