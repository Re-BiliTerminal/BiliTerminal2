package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.bangumi.BangumiDetail
import com.huanli233.biliwebapi.bean.bangumi.BangumiSections
import retrofit2.http.GET
import retrofit2.http.Query

interface IBangumiApi {
    
    @GET("/pgc/review/user")
    suspend fun getBangumiInfo(
        @Query("media_id") mediaId: Long
    ): ApiResponse<BangumiDetail>
    
    @GET("/pgc/web/season/section")
    suspend fun getBangumiSections(
        @Query("season_id") seasonId: Long
    ): ApiResponse<BangumiSections>
    
    @GET("/pgc/view/web/season")
    suspend fun getMediaIdFromEpId(
        @Query("ep_id") epId: Long
    ): ApiResponse<MediaIdResult>
    
    data class MediaIdResult(
        val media_id: Long
    )
}
