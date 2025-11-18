package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.UserRepository
import com.huanli233.biliwebapi.bean.user.UserArticle

class UserArticlePagingSource(
    private val mid: Long,
    private val repository: UserRepository
) : PagingSource<Int, UserArticle>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserArticle> {
        return try {
            val page = params.key ?: 1
            val result = repository.getUserArticles(mid, page)
            
            result.fold(
                onSuccess = { articles ->
                    LoadResult.Page(
                        data = articles,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (articles.isEmpty()) null else page + 1
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

    override fun getRefreshKey(state: PagingState<Int, UserArticle>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
