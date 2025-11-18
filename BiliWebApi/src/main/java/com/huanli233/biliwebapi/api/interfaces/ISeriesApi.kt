package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.series.SeriesInfo
import com.huanli233.biliwebapi.bean.series.UserSeriesList
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.GET
import retrofit2.http.Query

interface ISeriesApi {
    
    @GET("/x/series/archives")
    suspend fun getSeriesVideos(
        @Query("mid") mid: Long,
        @Query("series_id") seriesId: Long,
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 30
    ): ApiResponse<SeriesInfo>
    
    @GET("/x/polymer/web-space/seasons_archives_list")
    suspend fun getSeasonVideos(
        @Query("mid") mid: Long,
        @Query("season_id") seasonId: Long,
        @Query("page_num") page: Int = 1,
        @Query("page_size") pageSize: Int = 30
    ): ApiResponse<SeriesInfo>
    
    @GET("/x/polymer/web-space/seasons_series_list")
    @WbiSign
    suspend fun getUserSeriesList(
        @Query("mid") mid: Long,
        @Query("page_num") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<UserSeriesList>
}
