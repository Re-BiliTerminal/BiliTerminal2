package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.ItemResult
import com.huanli233.biliwebapi.bean.ListResult
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.biliwebapi.bean.opus.OpusCard
import com.huanli233.biliwebapi.bean.opus.OpusCardList
import com.huanli233.biliwebapi.bean.opus.OpusFavoriteRequest
import com.huanli233.biliwebapi.bean.opus.OpusLikeRequest
import com.huanli233.biliwebapi.httplib.Domains.MAIN_URL
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IOpusApi {

    @GET("/x/polymer/web-dynamic/v1/opus/detail")
    suspend fun getOpus(
        @Query("id") opusId: String,
        @Query("features") features: String = "itemOpusStyle,opusBigCover,onlyfansVote,endFooterHidden,decorationCard,onlyfansAssetsV2,ugcDelete,onlyfansQaCard,editable,opusPrivateVisible,avatarAutoTheme,htmlNewStyle"
    ): ApiResponse<ItemResult<Opus>>

    @POST("/x/dynamic/feed/dyn/thumb")
    @Csrf(forceQuery = true)
    suspend fun likeOpus(
        @Body body: OpusLikeRequest
    ): ApiResponse<Any>

    @POST("/x/community/cosmo/interface/simple_action")
    @Csrf(forceQuery = true)
    suspend fun favoriteOpus(
        @Body body: OpusFavoriteRequest
    ): ApiResponse<Any>

}