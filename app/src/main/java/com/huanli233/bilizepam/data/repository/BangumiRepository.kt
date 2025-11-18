package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IBangumiApi
import com.huanli233.biliwebapi.bean.bangumi.BangumiDetail
import com.huanli233.biliwebapi.bean.bangumi.BangumiSections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BangumiRepository @Inject constructor() {
    
    suspend fun getBangumiInfo(mediaId: Long): Result<BangumiDetail> {
        return bilibiliApi.api(IBangumiApi::class) {
            getBangumiInfo(mediaId)
        }.apiResultNonNull()
    }
    
    suspend fun getBangumiSections(seasonId: Long): Result<BangumiSections> {
        return bilibiliApi.api(IBangumiApi::class) {
            getBangumiSections(seasonId)
        }.apiResultNonNull()
    }
    
    suspend fun getMediaIdFromEpId(epId: Long): Result<Long> {
        return bilibiliApi.api(IBangumiApi::class) {
            getMediaIdFromEpId(epId)
        }.apiResultNonNull().map { it.media_id }
    }
}
