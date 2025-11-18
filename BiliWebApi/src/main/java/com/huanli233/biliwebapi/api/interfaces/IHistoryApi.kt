package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.history.HistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IHistoryApi {
    
    @GET("/x/web-interface/history/cursor")
    suspend fun getHistory(
        @Query("type") type: String = "archive",
        @Query("view_at") viewAt: Long = 0,
        @Query("business") business: String = "",
        @Query("max") max: Long = 0
    ): ApiResponse<HistoryResponse>
}
