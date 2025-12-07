package com.huanli233.biliwebapi.bean.video

data class FavoriteFolder(
    val id: Long = 0,
    val fid: Long = 0,
    val mid: Long = 0,
    val title: String = "",
    val cover: String = "",
    val mediaCount: Int = 0,
    val maxCount: Int = 999,
    val favState: Int = 0
)

data class FavoriteFoldersResponse(
    val count: Int = 0,
    val list: List<FavoriteFolder>? = null
)
