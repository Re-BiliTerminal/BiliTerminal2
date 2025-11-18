package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.FavoriteRepository
import com.huanli233.biliwebapi.bean.favorite.FavoriteVideo

class FavoriteVideosPagingSource(
    private val repository: FavoriteRepository,
    private val mid: Long,
    private val fid: Long
) : PagingSource<Int, FavoriteVideo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FavoriteVideo> {
        val page = params.key ?: 1

        return repository.getFavoriteVideos(mid, fid, page).fold(
            onSuccess = { response ->
                val videos = response.archives ?: emptyList()
                LoadResult.Page(
                    data = videos,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (videos.isEmpty()) null else page + 1
                )
            },
            onFailure = { error ->
                LoadResult.Error(error)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FavoriteVideo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
