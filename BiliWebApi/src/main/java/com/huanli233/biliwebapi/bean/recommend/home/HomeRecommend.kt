package com.huanli233.biliwebapi.bean.recommend.home

import android.os.Parcelable
import com.huanli233.biliwebapi.BiliWebApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeRecommend(
    val item: List<VideoInfo>,
    val mid: Long
) : Parcelable