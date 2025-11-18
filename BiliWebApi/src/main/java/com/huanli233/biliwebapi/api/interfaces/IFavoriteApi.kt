package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.favorite.FavoriteBoxListResponse
import com.huanli233.biliwebapi.bean.favorite.FavoriteVideosResponse
import com.huanli233.biliwebapi.bean.favorite.OpusFavoriteResponse
import com.huanli233.biliwebapi.httplib.Domains
import com.huanli233.biliwebapi.httplib.annotation.API
import retrofit2.http.GET
import retrofit2.http.Query

interface IFavoriteApi {
    
    @API(Domains.SPACE_URL)
    @GET("/ajax/fav/getBoxList")
    suspend fun getFavoriteBoxList(
        @Query("mid") mid: Long
    ): ApiResponse<FavoriteBoxListResponse>
    
    @GET("/x/space/fav/arc")
    suspend fun getFavoriteVideos(
        @Query("vmid") vmid: Long,
        @Query("fid") fid: Long,
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 30,
        @Query("tid") tid: Int = 0,
        @Query("keyword") keyword: String = "",
        @Query("order") order: String = "fav_time"
    ): ApiResponse<FavoriteVideosResponse>
    
    @GET("/x/polymer/web-dynamic/v1/opus/favlist")
    suspend fun getOpusFavoriteList(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<OpusFavoriteResponse>
}
