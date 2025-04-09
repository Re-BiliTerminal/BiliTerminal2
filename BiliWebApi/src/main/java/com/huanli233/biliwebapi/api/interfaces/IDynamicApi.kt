package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface IDynamicApi {

    @API(Domains.VC_API_URL)
    @POST("/dynamic_like/v1/dynamic_like/thumb")
    @FormUrlEncoded @Csrf
    suspend fun like(
        @Field("dynamic_id") id: String,
        @Field("up") action: Int
    ) : ApiResponse<Unit>

}