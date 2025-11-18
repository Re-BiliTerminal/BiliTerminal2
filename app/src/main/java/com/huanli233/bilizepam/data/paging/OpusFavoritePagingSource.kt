package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.FavoriteRepository
import com.huanli233.biliwebapi.bean.favorite.OpusFavoriteItem

class OpusFavoritePagingSource(
    private val repository: FavoriteRepository
) : PagingSource<Int, OpusFavoriteItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OpusFavoriteItem> {
        val page = params.key ?: 1

        return repository.getOpusFavoriteList(page).fold(
            onSuccess = { response ->
                val items = response.items ?: emptyList()
                LoadResult.Page(
                    data = items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (!response.hasMore) null else page + 1
                )
            },
            onFailure = { error ->
                LoadResult.Error(error)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, OpusFavoriteItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
