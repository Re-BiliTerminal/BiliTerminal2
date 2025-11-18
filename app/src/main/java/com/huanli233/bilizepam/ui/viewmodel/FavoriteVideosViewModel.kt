package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.FavoriteVideosPagingSource
import com.huanli233.bilizepam.data.repository.FavoriteRepository
import com.huanli233.biliwebapi.bean.favorite.FavoriteVideo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FavoriteVideosViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _folderParams = MutableStateFlow<Pair<Long, Long>?>(null)
    
    val videosFlow: Flow<PagingData<FavoriteVideo>> = _folderParams.flatMapLatest { params ->
        if (params == null) {
            kotlinx.coroutines.flow.flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false,
                    initialLoadSize = 30
                ),
                pagingSourceFactory = {
                    FavoriteVideosPagingSource(
                        repository = favoriteRepository,
                        mid = params.first,
                        fid = params.second
                    )
                }
            ).flow
        }
    }.cachedIn(viewModelScope)
    
    fun setFolder(mid: Long, fid: Long) {
        _folderParams.value = Pair(mid, fid)
    }
}
