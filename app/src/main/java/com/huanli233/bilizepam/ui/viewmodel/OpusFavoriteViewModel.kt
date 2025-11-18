package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.OpusFavoritePagingSource
import com.huanli233.bilizepam.data.repository.FavoriteRepository
import com.huanli233.biliwebapi.bean.favorite.OpusFavoriteItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class OpusFavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val opusFlow: Flow<PagingData<OpusFavoriteItem>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 10
        ),
        pagingSourceFactory = {
            OpusFavoritePagingSource(favoriteRepository)
        }
    ).flow.cachedIn(viewModelScope)
}
