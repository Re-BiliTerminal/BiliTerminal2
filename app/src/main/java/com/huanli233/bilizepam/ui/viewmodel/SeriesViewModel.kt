package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.data.paging.SeriesPagingSource
import com.huanli233.bilizepam.data.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {
    
    private val _videos = MutableStateFlow<Flow<PagingData<VideoInfo>>?>(null)
    val videos: StateFlow<Flow<PagingData<VideoInfo>>?> = _videos.asStateFlow()
    
    private val _seriesName = MutableStateFlow("")
    val seriesName: StateFlow<String> = _seriesName.asStateFlow()
    
    fun loadSeries(type: String, mid: Long, id: Long, name: String) {
        _seriesName.value = name
        _videos.value = Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                initialLoadSize = 30
            ),
            pagingSourceFactory = { SeriesPagingSource(repository, type, mid, id) }
        ).flow.cachedIn(viewModelScope)
    }
}
