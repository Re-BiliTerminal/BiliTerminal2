package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IFavoriteApi
import com.huanli233.biliwebapi.bean.favorite.FavoriteBoxListResponse
import com.huanli233.biliwebapi.bean.favorite.FavoriteVideosResponse
import com.huanli233.biliwebapi.bean.favorite.OpusFavoriteResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor() {
    
    suspend fun getFavoriteBoxList(mid: Long): Result<FavoriteBoxListResponse> {
        return bilibiliApi.api(IFavoriteApi::class) {
            getFavoriteBoxList(mid)
        }.apiResultNonNull()
    }
    
    suspend fun getFavoriteVideos(
        mid: Long,
        fid: Long,
        page: Int
    ): Result<FavoriteVideosResponse> {
        return bilibiliApi.api(IFavoriteApi::class) {
            getFavoriteVideos(vmid = mid, fid = fid, page = page)
        }.apiResultNonNull()
    }
    
    suspend fun getOpusFavoriteList(page: Int): Result<OpusFavoriteResponse> {
        return bilibiliApi.api(IFavoriteApi::class) {
            getOpusFavoriteList(page = page)
        }.apiResultNonNull()
    }
}
