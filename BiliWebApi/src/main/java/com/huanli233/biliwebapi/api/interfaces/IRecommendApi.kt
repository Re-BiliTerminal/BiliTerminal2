package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.ListResult
import com.huanli233.biliwebapi.bean.recommend.home.HomeRecommend
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.httplib.annotation.Queries
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.GET
import retrofit2.http.Query

interface IRecommendApi {
    @GET("/x/web-interface/wbi/index/top/feed/rcmd")
    @WbiSign @Queries(keys = ["web_location", "feed_version", "homepage_ver", "screen", "seo_info"], values = ["1430650", "V8", "1", "1100-2056", ""])
    suspend fun getRecommend(
        @Query("fresh_type") freshType: Int = 3,
        @Query("uniq_id") uniqId: String = "",
        @Query("ps") pageSize: Int = 15,
        @Query("fresh_idx") freshIndex: Int = 1,
        @Query("fresh_idx_1h") freshIndex1h: Int = 1,
        @Query("brush") brush: Int = 1,
    ): ApiResponse<HomeRecommend>

    @GET("/x/web-interface/archive/related")
    suspend fun getRelated(@Query("aid") aid: Long, @Query("bvid") bvid: String): ApiResponse<List<VideoInfo>>

    @GET("/x/web-interface/popular/precious")
    suspend fun getPrecious(@Query("page") page: Int, @Query("page_size") pageSize: Int = 10): ApiResponse<ListResult<VideoInfo>>

    @GET("/x/web-interface/popular")
    suspend fun getPopular(@Query("pn") page: Int, @Query("ps") pageSize: Int = 10): ApiResponse<ListResult<VideoInfo>>
}