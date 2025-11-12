package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.DynamicPagingSource
import com.huanli233.bilizepam.data.repository.DynamicRepository
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DynamicViewModel @Inject constructor(
    private val repository: DynamicRepository
) : ViewModel() {
    
    val dynamicFlow: Flow<PagingData<Dynamic>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = { DynamicPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)
}
