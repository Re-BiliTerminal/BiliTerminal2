package com.huanli233.bilizepam.data.repository

import com.huanli233.biliwebapi.api.interfaces.IOpusApi
import com.huanli233.biliwebapi.bean.ItemResult
import com.huanli233.biliwebapi.bean.opus.Opus
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.bilizepam.data.setting.LocalData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpusRepository @Inject constructor() {
    suspend fun getOpusDetail(opusId: String): Result<ItemResult<Opus>> {
        return bilibiliApi.api(IOpusApi::class) {
            getOpus(opusId)
        }.apiResultNonNull()
    }
    
    suspend fun likeOpus(opusId: String, isLike: Boolean): Result<Unit> {
        val request = com.huanli233.biliwebapi.bean.opus.OpusLikeRequest(
            dynIdStr = opusId,
            up = if (isLike) 1 else 2
        )
        
        return bilibiliApi.api(IOpusApi::class) {
            likeOpus(body = request)
        }.apiResultNonNull().map { }
    }
    
    suspend fun favoriteOpus(opusId: String, isFavorite: Boolean): Result<Unit> {
        val request = com.huanli233.biliwebapi.bean.opus.OpusFavoriteRequest(
            meta = com.huanli233.biliwebapi.bean.opus.FavoriteMeta(),
            entity = com.huanli233.biliwebapi.bean.opus.FavoriteEntity(
                objectIdStr = opusId,
                type = com.huanli233.biliwebapi.bean.opus.FavoriteType()
            ),
            action = if (isFavorite) 3 else 4
        )
        
        return bilibiliApi.api(IOpusApi::class) {
            favoriteOpus(body = request)
        }.apiResultNonNull().map { }
    }
}
