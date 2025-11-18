package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.paging.FollowingPagingSource
import com.huanli233.bilizepam.data.repository.FollowRepository
import com.huanli233.biliwebapi.bean.follow.FollowUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _mid = MutableStateFlow<Long?>(null)
    
    val followingFlow: Flow<PagingData<FollowUser>> = _mid.flatMapLatest { mid ->
        if (mid == null) {
            kotlinx.coroutines.flow.flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    FollowingPagingSource(
                        repository = followRepository,
                        mid = mid
                    )
                }
            ).flow
        }
    }.cachedIn(viewModelScope)
    
    fun setMid(mid: Long) {
        _mid.value = mid
    }
}
