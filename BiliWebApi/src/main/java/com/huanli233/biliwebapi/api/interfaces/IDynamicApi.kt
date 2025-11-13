package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.ItemResult
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.biliwebapi.bean.dynamic.DynamicFeedResponse
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.Queries
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IDynamicApi {

    @GET("/x/polymer/web-dynamic/v1/feed/all")
    suspend fun getDynamicFeed(
        @Query("timezone_offset") timezoneOffset: String = "-480",
        @Query("type") type: String = "all",
        @Query("platform") platform: String = "web",
        @Query("offset") offset: String? = null,
        @Query("page") page: Int = 1,
        @Query("features") features: String = "itemOpusStyle,listOnlyfans,opusBigCover,onlyfansVote,decorationCard,onlyfansAssetsV2,forwardListHidden,ugcDelete,onlyfansQaCard,commentsNewVersion,avatarAutoTheme",
        @Query("web_location") webLocation: String = "333.1365"
    ): ApiResponse<DynamicFeedResponse>

    @GET("/x/polymer/web-dynamic/v1/detail")
    @Queries(
        keys = ["features"],
        values = ["itemOpusStyle,opusBigCover,onlyfansVote,endFooterHidden,decorationCard,onlyfansAssetsV2,ugcDelete,onlyfansQaCard,editable,opusPrivateVisible,avatarAutoTheme"]
    )
    suspend fun getDynamic(@Query("id") id: String) : ApiResponse<ItemResult<Dynamic>>

    @API(Domains.VC_API_URL)
    @POST("/dynamic_like/v1/dynamic_like/thumb")
    @FormUrlEncoded @Csrf
    suspend fun like(
        @Field("dynamic_id") id: String,
        @Field("up") action: Int
    ) : ApiResponse<Unit>

}