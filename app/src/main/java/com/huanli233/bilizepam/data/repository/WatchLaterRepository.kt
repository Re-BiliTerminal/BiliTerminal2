package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IWatchLaterApi
import com.huanli233.biliwebapi.bean.watchlater.WatchLaterResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchLaterRepository @Inject constructor() {
    
    suspend fun getWatchLaterList(): Result<WatchLaterResponse> {
        return bilibiliApi.api(IWatchLaterApi::class) {
            getWatchLaterList()
        }.apiResultNonNull()
    }
    
    suspend fun addToWatchLater(aid: Long): Result<Unit> {
        return bilibiliApi.api(IWatchLaterApi::class) {
            addToWatchLater(aid)
        }.apiResultNonNull().map { }
    }
    
    suspend fun deleteFromWatchLater(aid: Long): Result<Unit> {
        return bilibiliApi.api(IWatchLaterApi::class) {
            deleteFromWatchLater(aid)
        }.apiResultNonNull().map { }
    }
}
