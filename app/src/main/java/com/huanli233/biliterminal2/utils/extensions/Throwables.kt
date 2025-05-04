package com.huanli233.biliterminal2.utils.extensions

import com.huanli233.biliterminal2.api.BilibiliApiException

fun Throwable.msg() =
    if (this is BilibiliApiException && cause == null) {
        "$code $message"
    } else {
        toString()
    }