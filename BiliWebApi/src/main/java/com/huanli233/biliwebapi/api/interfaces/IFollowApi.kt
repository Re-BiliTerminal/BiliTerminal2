package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.follow.FollowListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface IFollowApi {
    
    @GET("/x/relation/followings")
    suspend fun getFollowingList(
        @Query("vmid") vmid: Long,
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 20,
        @Query("order") order: String = "desc",
        @Query("order_type") orderType: String = "attention"
    ): ApiResponse<FollowListResponse>
    
    @GET("/x/relation/followers")
    suspend fun getFollowerList(
        @Query("vmid") vmid: Long,
        @Query("pn") page: Int = 1,
        @Query("ps") pageSize: Int = 20,
        @Query("order") order: String = "desc",
        @Query("order_type") orderType: String = "attention"
    ): ApiResponse<FollowListResponse>
}
