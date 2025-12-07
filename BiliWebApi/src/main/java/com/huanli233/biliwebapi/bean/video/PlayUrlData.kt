package com.huanli233.biliwebapi.bean.video

data class PlayUrlData(
    val durl: List<DurlItem>? = null,
    val quality: Int = 0,
    val acceptQuality: List<Int>? = null,
    val acceptDescription: List<String>? = null
)

data class DurlItem(
    val url: String = "",
    val size: Long = 0,
    val length: Long = 0
)
