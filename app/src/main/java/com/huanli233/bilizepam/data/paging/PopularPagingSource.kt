package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.data.repository.RecommendRepository

class PopularPagingSource(
    private val repository: RecommendRepository
) : PagingSource<Int, VideoInfo>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoInfo> {
        return try {
            val page = params.key ?: 1
            
            val result = repository.getPopular(page, params.loadSize)
            
            result.fold(
                onSuccess = { listResult ->
                    val videos = listResult.items
                    LoadResult.Page(
                        data = videos,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (videos.isEmpty()) null else page + 1
                    )
                },
                onFailure = { exception ->
                    LoadResult.Error(exception)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, VideoInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
