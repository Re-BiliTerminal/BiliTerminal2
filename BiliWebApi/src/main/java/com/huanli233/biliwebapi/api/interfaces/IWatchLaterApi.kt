package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.watchlater.WatchLaterResponse
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface IWatchLaterApi {
    
    @GET("/x/v2/history/toview/web")
    suspend fun getWatchLaterList(): ApiResponse<WatchLaterResponse>
    
    @POST("/x/v2/history/toview/add")
    @FormUrlEncoded
    @Csrf
    suspend fun addToWatchLater(
        @Field("aid") aid: Long
    ): ApiResponse<Any>
    
    @POST("/x/v2/history/toview/del")
    @FormUrlEncoded
    @Csrf
    suspend fun deleteFromWatchLater(
        @Field("aid") aid: Long
    ): ApiResponse<Any>
}
