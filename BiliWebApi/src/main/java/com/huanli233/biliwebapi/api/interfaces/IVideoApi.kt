package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.video.PlayerInfo
import com.huanli233.biliwebapi.bean.video.SubtitleContent
import com.huanli233.biliwebapi.bean.video.VideoInfo
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IVideoApi {
    @GET("/x/web-interface/view")
    suspend fun getVideoInfo(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<VideoInfo>

    @GET("/x/player/wbi/v2")
    suspend fun getPlayerInfo(@Query("aid") aid: Long, @Query("cid") cid: Long): ApiResponse<PlayerInfo>

    @GET
    suspend fun getSubtitleContent(@Url url: String): ApiResponse<SubtitleContent>
}