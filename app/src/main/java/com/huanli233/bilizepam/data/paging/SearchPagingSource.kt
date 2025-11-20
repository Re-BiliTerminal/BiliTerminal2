package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.SearchRepository
import com.huanli233.biliwebapi.bean.search.SearchItem

class SearchPagingSource(
    private val searchRepository: SearchRepository,
    private val keyword: String,
    private val searchType: String
) : PagingSource<Int, SearchItem>() {
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchItem> {
        return try {
            val page = params.key ?: 1
            
            val result = when (searchType) {
                "video" -> searchRepository.searchVideos(keyword, page)
                "article" -> searchRepository.searchArticles(keyword, page)
                "bili_user" -> searchRepository.searchUsers(keyword, page)
                "live" -> searchRepository.searchLive(keyword, page)
                else -> searchRepository.searchVideos(keyword, page)
            }
            
            val items = result.getOrThrow()
            
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, SearchItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
