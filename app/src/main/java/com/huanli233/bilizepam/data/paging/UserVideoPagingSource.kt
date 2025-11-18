package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.UserRepository
import com.huanli233.biliwebapi.bean.video.VideoInfo

class UserVideoPagingSource(
    private val mid: Long,
    private val repository: UserRepository
) : PagingSource<Int, VideoInfo>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoInfo> {
        return try {
            val page = params.key ?: 1
            val result = repository.getUserVideos(mid, page)
            
            result.fold(
                onSuccess = { videos ->
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
