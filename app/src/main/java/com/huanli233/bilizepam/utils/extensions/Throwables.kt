package com.huanli233.bilizepam.utils.extensions

import com.huanli233.bilizepam.api.BilibiliApiException

fun Throwable.msg() =
    if (this is BilibiliApiException && cause == null) {
        "$code $message"
    } else {
        toString()
    }