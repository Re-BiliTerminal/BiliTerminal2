package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.ItemResult
import com.huanli233.biliwebapi.bean.ListResult
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.biliwebapi.bean.opus.OpusCard
import com.huanli233.biliwebapi.bean.opus.OpusCardList
import com.huanli233.biliwebapi.httplib.Domains.MAIN_URL
import com.huanli233.biliwebapi.httplib.annotation.API
import retrofit2.http.GET
import retrofit2.http.Query

interface IOpusApi {

    @GET("/x/polymer/web-dynamic/v1/opus/detail")
    suspend fun getOpus(@Query("opusId") opusId: String): ApiResponse<ItemResult<Opus>>

    @GET("/x/polymer/web-dynamic/v1/opus/favlist")
    suspend fun getFavList(
        @Query("page_size") pageSize: Int = 10,
        @Query("page") page: Int
    ): ApiResponse<OpusCardList>

}