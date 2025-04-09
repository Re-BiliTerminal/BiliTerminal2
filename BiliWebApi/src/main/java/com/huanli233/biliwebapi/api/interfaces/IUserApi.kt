package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.user.UserCard
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import retrofit2.http.GET
import retrofit2.http.Query

interface IUserApi {

    @GET("/x/web-interface/card")
    suspend fun getCard(
        @Query("mid") userId: String
    ): ApiResponse<UserCardInfo>

    @GET("/x/space/notice")
    suspend fun getNotice(
        @Query("mid") userId: String
    ): ApiResponse<String>

}