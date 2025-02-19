package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.request_param.BiliTicket
import com.huanli233.biliwebapi.bean.request_param.Buvids
import com.huanli233.biliwebapi.httplib.annotation.Queries
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface IRequestParamApi {
    @GET("/x/frontend/finger/spi")
    suspend fun requestBuvids(): ApiResponse<Buvids>

    @POST("/bapis/bilibili.api.ticket.v1.Ticket/GenWebTicket")
    @Queries(keys = ["csrf"], values = [""])
    suspend fun genWebTicket(
        @Query("key_id") keyId: String?,
        @Query("hexsign") hexsign: String?,
        @Query("context[ts]") ts: String?
    ): ApiResponse<BiliTicket>
}