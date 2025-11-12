package com.huanli233.bilizepam.data.repository

import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IDynamicApi
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.biliwebapi.bean.dynamic.DynamicFeedResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicRepository @Inject constructor() {
    
    suspend fun getDynamicFeed(
        offset: String? = null,
        type: String = "all"
    ): Result<DynamicFeedResponse> {
        return bilibiliApi.api(IDynamicApi::class) {
            getDynamicFeed(offset = offset, type = type)
        }.apiResultNonNull()
    }
    
    suspend fun getDynamic(id: String): Result<Dynamic> {
        return bilibiliApi.api(IDynamicApi::class) {
            getDynamic(id)
        }.apiResultNonNull().mapCatching { it.item }
    }
}
