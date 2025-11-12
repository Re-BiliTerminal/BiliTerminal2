package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.DynamicRepository
import com.huanli233.biliwebapi.bean.dynamic.Dynamic

class DynamicPagingSource(
    private val repository: DynamicRepository
) : PagingSource<String, Dynamic>() {
    
    override suspend fun load(params: LoadParams<String>): LoadResult<String, Dynamic> {
        return try {
            val offset = params.key
            val result = repository.getDynamicFeed(offset = offset)
            
            result.fold(
                onSuccess = { response ->
                    val items = response.items ?: emptyList()
                    LoadResult.Page(
                        data = items,
                        prevKey = null,
                        nextKey = if (response.hasMore) response.offset else null
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
    
    override fun getRefreshKey(state: PagingState<String, Dynamic>): String? {
        return null
    }
}
