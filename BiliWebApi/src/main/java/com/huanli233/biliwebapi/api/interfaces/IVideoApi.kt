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

    @GET("/x/player/wbi/playurl")
    suspend fun getPlayUrl(
        @Query("avid") aid: Long,
        @Query("cid") cid: Long,
        @Query("qn") qn: Int = 64,
        @Query("fnval") fnval: Int = 1,
        @Query("fnver") fnver: Int = 0,
        @Query("platform") platform: String = "pc"
    ): ApiResponse<PlayUrlData>

    @GET("/x/web-interface/archive/relation")
    suspend fun getVideoRelation(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<VideoRelation>

    @GET("/x/tag/archive/tags")
    suspend fun getVideoTags(@Query("aid") aid: Long, @Query("bvid") bvid: String = ""): ApiResponse<List<Tag>>

    /**
     * @param action 1:点赞 2:取消点赞
     */
    @POST("/x/web-interface/archive/like")
    @FormUrlEncoded
    suspend fun likeVideo(
        @Field("aid") aid: Long,
        @Field("like") action: Int
    ): ApiResponse<Unit>

    @GET
    suspend fun getSubtitleContent(@Url url: String): ApiResponse<SubtitleContent>

    /**
     * 投币
     * @param aid 视频aid
     * @param multiply 投币数量 1-2
     * @param selectLike 是否同时点赞 1=点赞 0=不点赞
     */
    @POST("/x/web-interface/coin/add")
    @FormUrlEncoded
    @Csrf
    suspend fun coinVideo(
        @Field("aid") aid: Long,
        @Field("multiply") multiply: Int,
        @Field("select_like") selectLike: Int = 0
    ): ApiResponse<Unit>

    /**
     * 添加到稍后再看
     * @param aid 视频aid
     */
    @POST("/x/v2/history/toview/add")
    @FormUrlEncoded
    @Csrf
    suspend fun addToWatchLater(
        @Field("aid") aid: Long
    ): ApiResponse<Unit>

    /**
     * 获取收藏夹列表（带收藏状态）
     * @param rid 视频aid
     * @param upMid UP主mid
     */
    @GET("/x/v3/fav/folder/created/list-all")
    suspend fun getFavoriteFolders(
        @Query("type") type: Int = 2,
        @Query("rid") rid: Long,
        @Query("up_mid") upMid: Long
    ): ApiResponse<FavoriteFoldersResponse>

    /**
     * 添加/删除收藏
     * @param rid 视频aid
     * @param type 类型 2=视频
     * @param addMediaIds 要添加的收藏夹ID（多个用逗号分隔，需要加上mid后两位）
     * @param delMediaIds 要删除的收藏夹ID（多个用逗号分隔，需要加上mid后两位）
     */
    @POST("/medialist/gateway/coll/resource/deal")
    @FormUrlEncoded
    @Csrf
    suspend fun updateFavorite(
        @Field("rid") rid: Long,
        @Field("type") type: Int = 2,
        @Field("add_media_ids") addMediaIds: String = "",
        @Field("del_media_ids") delMediaIds: String = ""
    ): ApiResponse<Unit>
}

data class FavoriteFoldersResponse(
    val count: Int = 0,
    val list: List<FavoriteFolder>? = null
)

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