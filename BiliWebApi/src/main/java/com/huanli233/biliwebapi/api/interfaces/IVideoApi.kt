package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.video.PlayerInfo
import com.huanli233.biliwebapi.bean.video.SubtitleContent
import com.huanli233.biliwebapi.bean.video.Tag
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.bean.video.VideoRelation
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.Fields
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface IVideoApi {
    @GET("/x/web-interface/view")
    suspend fun getVideoInfo(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<VideoInfo>

    @GET("/x/player/wbi/v2")
    suspend fun getPlayerInfo(@Query("aid") aid: Long, @Query("cid") cid: Long): ApiResponse<PlayerInfo>

    @GET("/x/web-interface/archive/relation")
    suspend fun getVideoRelation(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<VideoRelation>

    @GET("/x/tag/archive/tags")
    suspend fun getVideoTags(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<List<Tag>>

    /**
     * @param action 1:点赞 2:取消点赞
     */
    @POST("/x/web-interface/archive/like")
    @FormUrlEncoded
    @Csrf @Fields(keys = ["from_spmid", "spmid"], values = ["333.1007.tianma.1-1-1.click", "333.788.0.0"])
    suspend fun likeVideo(
        @Field("aid") aid: Long,
        @Field("like") action: Int
    ): ApiResponse<Unit>

    @GET
    suspend fun getSubtitleContent(@Url url: String): ApiResponse<SubtitleContent>
}