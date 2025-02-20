package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.recommend.home.HomeRecommend
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.httplib.annotation.Queries
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.GET
import retrofit2.http.Query

interface IRecommendApi {
    @GET("/x/web-interface/wbi/index/top/feed/rcmd")
    @WbiSign @Queries(keys = ["web_location", "feed_version", "homepage_ver", "screen"], values = ["1430650", "V8", "1", "1100-2056"])
    suspend fun getRecommend(@Query("uniq_id") uniqId: String): ApiResponse<HomeRecommend>

    @GET("/x/web-interface/archive/related")
    suspend fun getRelated(@Query("aid") aid: Long, @Query("bvid") bvid: String): ApiResponse<List<VideoInfo>>
}