package com.huanli233.bilizepam.ui.screens.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import com.huanli233.biliwebapi.bean.video.VideoInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor() : ViewModel() {

    val videos: Flow<PagingData<VideoInfo>> = Pager(
        config = PagingConfig(
            pageSize = 15,
            enablePlaceholders = false,
            initialLoadSize = 15
        ),
        pagingSourceFactory = { RecommendPagingSource() }
    ).flow.cachedIn(viewModelScope)
}

class RecommendPagingSource : PagingSource<Int, VideoInfo>() {
    
    private var lastUniqId: String = ""
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoInfo> {
        return try {
            val freshIndex = params.key ?: 1
            
            val result = bilibiliApi.api(IRecommendApi::class) {
                getRecommend(
                    freshType = 3,
                    uniqId = lastUniqId,
                    pageSize = params.loadSize,
                    freshIndex = freshIndex,
                    freshIndex1h = freshIndex,
                    brush = freshIndex
                )
            }.apiResultNonNull()
            
            val response = result.getOrThrow()
            val videos = response.item
            
            // Update uniqId for next page - using a simple hash of current items
            if (videos.isNotEmpty()) {
                lastUniqId = videos.joinToString("") { it.aid.toString() }
                    .hashCode()
                    .toString()
            }
            
            LoadResult.Page(
                data = videos,
                prevKey = null, // Only forward pagination
                nextKey = if (videos.isEmpty()) null else freshIndex + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, VideoInfo>): Int? {
        return null // Always start from the beginning on refresh
    }
}
