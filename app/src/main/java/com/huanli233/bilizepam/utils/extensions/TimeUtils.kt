package com.huanli233.bilizepam.utils.extensions

fun Long.formatToRelativeTime(): String {
    val now = System.currentTimeMillis()
    val timestampInMillis = if (this.toString().length == 10) {
        this * 1000
    } else {
        this
    }
    
    val diff = now - timestampInMillis
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> "${years}年前"
        months > 0 -> "${months}个月前"
        days > 0 -> "${days}天前"
        hours > 0 -> "${hours}小时前"
        minutes > 0 -> "${minutes}分钟前"
        else -> "刚刚"
    }
}
