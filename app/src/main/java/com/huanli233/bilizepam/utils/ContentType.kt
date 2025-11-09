package com.huanli233.bilizepam.utils

/**
 * 内容类型常量，用于评论系统
 * 参考BiliClient项目的ContentType
 */
object ContentType {
    const val VIDEO = 1           // 视频
    const val ARTICLE = 12        // 专栏文章
    const val DYNAMIC = 17        // 动态
    const val AUDIO = 14          // 音频
    const val LIVE = 1            // 直播（与视频相同）
    const val BANGUMI = 1         // 番剧（与视频相同）
    const val MOVIE = 1           // 电影（与视频相同）
    
    /**
     * 获取内容类型的显示名称
     */
    fun getTypeName(type: Int): String {
        return when (type) {
            VIDEO -> "视频"
            ARTICLE -> "专栏"
            DYNAMIC -> "动态"
            AUDIO -> "音频"
            else -> "内容"
        }
    }
    
    /**
     * 检查类型是否有效
     */
    fun isValidType(type: Int): Boolean {
        return type in listOf(VIDEO, ARTICLE, DYNAMIC, AUDIO)
    }
}
