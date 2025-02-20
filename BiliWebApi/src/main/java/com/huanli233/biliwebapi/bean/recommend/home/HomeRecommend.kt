package com.huanli233.biliwebapi.bean.recommend.home

import com.google.gson.annotations.Expose
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.video.VideoInfo

data class HomeRecommend(
    @Expose val item: List<VideoInfo>,
    @Expose val mid: Long
) {
    companion object {
        suspend fun request(api: BiliWebApi, uniqId: String): ApiResponse<HomeRecommend> {
            return api.getApi(IRecommendApi::class.java).getRecommend(uniqId)
        }
    }
}
