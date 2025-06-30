package com.huanli233.bilizepam.utils

object Patterns {
    const val URL_PATTERN = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"
    const val BV_PATTERN = "BV[A-Za-z0-9]{10}"
    const val AV_PATTERN = "av\\d{1,10}"
    const val CV_PATTERN = "cv\\d{1,10}"
    const val UID_PATTERN = "^(?i)uid\\d+$"
}