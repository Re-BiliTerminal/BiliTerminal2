package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IFollowApi
import com.huanli233.biliwebapi.bean.follow.FollowListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepository @Inject constructor() {
    
    suspend fun getFollowingList(mid: Long, page: Int): Result<FollowListResponse> {
        return bilibiliApi.api(IFollowApi::class) {
            getFollowingList(vmid = mid, page = page)
        }.apiResultNonNull()
    }
    
    suspend fun getFollowerList(mid: Long, page: Int): Result<FollowListResponse> {
        return bilibiliApi.api(IFollowApi::class) {
            getFollowerList(vmid = mid, page = page)
        }.apiResultNonNull()
    }
}
