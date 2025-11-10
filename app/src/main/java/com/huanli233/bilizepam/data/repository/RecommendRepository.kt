package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.video.VideoInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendRepository @Inject constructor() {
    
    suspend fun getRelatedVideos(aid: Long, bvid: String = ""): Result<List<VideoInfo>> {
        return bilibiliApi.api(IRecommendApi::class) {
            getRelated(aid, bvid)
        }.apiResultNonNull()
    }
    
    suspend fun getHomeRecommend(
        freshType: Int = 3,
        uniqId: String = "",
        pageSize: Int = 15
    ): Result<com.huanli233.biliwebapi.bean.recommend.home.HomeRecommend> {
        return bilibiliApi.api(IRecommendApi::class) {
            getRecommend(freshType, uniqId, pageSize)
        }.apiResultNonNull()
    }
    
    suspend fun getPopularVideos(page: Int, pageSize: Int = 10): Result<List<VideoInfo>> {
        return bilibiliApi.api(IRecommendApi::class) {
            getPopular(page, pageSize)
        }.apiResultNonNull().map { it.items }
    }
    
    suspend fun getPreciousVideos(page: Int, pageSize: Int = 10): Result<List<VideoInfo>> {
        return bilibiliApi.api(IRecommendApi::class) {
            getPrecious(page, pageSize)
        }.apiResultNonNull().map { it.items }
    }
}
