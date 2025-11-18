package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.FollowRepository
import com.huanli233.biliwebapi.bean.follow.FollowUser

class FollowingPagingSource(
    private val repository: FollowRepository,
    private val mid: Long
) : PagingSource<Int, FollowUser>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FollowUser> {
        val page = params.key ?: 1

        return repository.getFollowingList(mid, page).fold(
            onSuccess = { response ->
                val users = response.list ?: emptyList()
                LoadResult.Page(
                    data = users,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (users.isEmpty()) null else page + 1
                )
            },
            onFailure = { error ->
                LoadResult.Error(error)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FollowUser>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
