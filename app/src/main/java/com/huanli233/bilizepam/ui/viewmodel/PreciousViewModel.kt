package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.data.paging.PreciousPagingSource
import com.huanli233.bilizepam.data.repository.RecommendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PreciousViewModel @Inject constructor(
    private val repository: RecommendRepository
) : ViewModel() {
    
    val videos: Flow<PagingData<VideoInfo>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 10
        ),
        pagingSourceFactory = { PreciousPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)
}
