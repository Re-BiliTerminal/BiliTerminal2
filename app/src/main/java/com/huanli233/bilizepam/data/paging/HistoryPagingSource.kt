package com.huanli233.bilizepam.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.HistoryRepository
import com.huanli233.biliwebapi.bean.history.HistoryItem

class HistoryPagingSource(
    private val historyRepository: HistoryRepository
) : PagingSource<HistoryCursor, HistoryItem>() {

    override fun getRefreshKey(state: PagingState<HistoryCursor, HistoryItem>): HistoryCursor? {
        return null
    }

    override suspend fun load(params: LoadParams<HistoryCursor>): LoadResult<HistoryCursor, HistoryItem> {
        val cursor = params.key ?: HistoryCursor()
        
        return historyRepository.getHistory(
            viewAt = cursor.viewAt,
            business = cursor.business,
            max = cursor.max
        ).fold(
            onSuccess = { response ->
                val nextCursor = if (response.list.isEmpty()) {
                    null
                } else {
                    HistoryCursor(
                        viewAt = response.cursor.viewAt,
                        business = response.cursor.business,
                        max = response.cursor.max
                    )
                }
                
                LoadResult.Page(
                    data = response.list,
                    prevKey = null,
                    nextKey = nextCursor
                )
            },
            onFailure = { error ->
                LoadResult.Error(error)
            }
        )
    }
}

data class HistoryCursor(
    val viewAt: Long = 0,
    val business: String = "",
    val max: Long = 0
)
