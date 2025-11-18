package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.ISeriesApi
import com.huanli233.biliwebapi.bean.series.SeriesInfo
import com.huanli233.biliwebapi.bean.series.UserSeriesList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepository @Inject constructor() {
    
    suspend fun getSeriesVideos(
        mid: Long,
        seriesId: Long,
        page: Int = 1,
        pageSize: Int = 30
    ): Result<SeriesInfo> {
        return bilibiliApi.api(ISeriesApi::class) {
            getSeriesVideos(mid, seriesId, page, pageSize)
        }.apiResultNonNull()
    }
    
    suspend fun getSeasonVideos(
        mid: Long,
        seasonId: Long,
        page: Int = 1,
        pageSize: Int = 30
    ): Result<SeriesInfo> {
        return bilibiliApi.api(ISeriesApi::class) {
            getSeasonVideos(mid, seasonId, page, pageSize)
        }.apiResultNonNull()
    }
    
    suspend fun getUserSeriesList(
        mid: Long,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<UserSeriesList> {
        return bilibiliApi.api(ISeriesApi::class) {
            getUserSeriesList(mid, page, pageSize)
        }.apiResultNonNull()
    }
}
