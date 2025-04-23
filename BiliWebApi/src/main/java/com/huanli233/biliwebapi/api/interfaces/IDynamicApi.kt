package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.ItemResult
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IDynamicApi {

    @GET("/x/polymer/web-dynamic/v1/detail")
    suspend fun getDynamic(@Query("id") id: String) : ApiResponse<ItemResult<Dynamic>>

    @API(Domains.VC_API_URL)
    @POST("/dynamic_like/v1/dynamic_like/thumb")
    @FormUrlEncoded @Csrf
    suspend fun like(
        @Field("dynamic_id") id: String,
        @Field("up") action: Int
    ) : ApiResponse<Unit>

}