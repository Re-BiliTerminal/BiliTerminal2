package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.user.NavUserInfo
import com.huanli233.biliwebapi.bean.user.UserArticleListResponse
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import com.huanli233.biliwebapi.bean.user.UserVideoListResponse
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.DmImg
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IUserApi {

    @GET("/x/web-interface/nav")
    suspend fun getMyInfo(): ApiResponse<NavUserInfo>

    @GET("/x/web-interface/card")
    suspend fun getCard(
        @Query("mid") userId: String
    ): ApiResponse<UserCardInfo>

    @GET("/x/space/notice")
    suspend fun getNotice(
        @Query("mid") userId: String
    ): ApiResponse<String>

    @WbiSign
    @DmImg
    @GET("/x/space/wbi/arc/search")
    suspend fun getUserVideos(
        @Query("mid") mid: Long,
        @Query("pn") page: Int,
        @Query("ps") pageSize: Int = 20,
        @Query("order") order: String = "pubdate"
    ): ApiResponse<UserVideoListResponse>

    @WbiSign
    @GET("/x/space/wbi/article")
    suspend fun getUserArticles(
        @Query("mid") mid: Long,
        @Query("pn") page: Int,
        @Query("ps") pageSize: Int = 30,
        @Query("order") order: String = "pubdate",
        @Query("order_avoided") orderAvoided: Boolean = true,
        @Query("tid") tid: Int = 0
    ): ApiResponse<UserArticleListResponse>
    
    @FormUrlEncoded
    @POST("/x/relation/modify")
    @DmImg
    @Csrf
    suspend fun followUser(
        @Field("fid") mid: Long,
        @Field("act") act: Int,
    ): ApiResponse<Any>
}