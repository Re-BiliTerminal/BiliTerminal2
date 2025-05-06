package com.huanli233.biliterminal2.ui.fragment.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.huanli233.biliterminal2.ui.fragment.recommend.RecommendPagingSource

class RecommendViewModel: ViewModel() {

    val pagingConfig = PagingConfig(
        pageSize = 15,
        enablePlaceholders = false,
        prefetchDistance = 3
    )

    val videos = Pager(
        config = pagingConfig,
        pagingSourceFactory = { RecommendPagingSource() }
    ).flow.cachedIn(viewModelScope)

}