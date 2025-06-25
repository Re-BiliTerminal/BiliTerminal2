package com.huanli233.bilizepam.utils.extensions

import com.elvishew.xlog.XLog
import com.huanli233.bilizepam.utils.locale.LocaleDelegate
import java.text.SimpleDateFormat
import java.util.Date

fun Long.formatToDate(
    format: String = "yyyy-MM-dd HH:mm:ss"
): String {
    val timestampInMillis = if (this.toString().length == 10) {
        this * 1000
    } else {
        this
    }

    return try {
        SimpleDateFormat(format, LocaleDelegate.defaultLocale).format(Date(timestampInMillis))
    } catch (e: Exception) {
        XLog.e(e)
        this.toString()
    }
}

fun Number.formatNumber(
    thousand: String,
    million: String,
): String {
    val num = this.toLong()
    return when {
        num >= 100_000_000 -> "%.1f${million}".format(LocaleDelegate.defaultLocale, num / 100_000_000.0)
        num >= 10_000 -> "%.1f${thousand}".format(LocaleDelegate.defaultLocale, num / 10_000.0)
        else -> num.toString()
    }
}

fun toTime(progress: Long): String {
    val hours = progress / 3600
    val minutes = (progress % 3600) / 60
    val seconds = progress % 60

    return if (hours > 0) {
        String.format(LocaleDelegate.defaultLocale, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(LocaleDelegate.defaultLocale, "%02d:%02d", minutes, seconds)
    }
}