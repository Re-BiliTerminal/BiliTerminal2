package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.HistoryPagingSource
import com.huanli233.bilizepam.data.repository.HistoryRepository
import com.huanli233.biliwebapi.bean.history.HistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val historyFlow: Flow<PagingData<HistoryItem>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = {
            HistoryPagingSource(historyRepository)
        }
    ).flow.cachedIn(viewModelScope)
}
