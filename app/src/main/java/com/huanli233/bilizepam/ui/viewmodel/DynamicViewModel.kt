package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.huanli233.bilizepam.data.paging.DynamicPagingSource
import com.huanli233.bilizepam.data.repository.DynamicRepository
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DynamicViewModel @Inject constructor(
    private val repository: DynamicRepository
) : ViewModel() {
    
    private val likeStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    
    private val baseDynamicFlow: Flow<PagingData<Dynamic>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20
        ),
        pagingSourceFactory = { DynamicPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)
    
    val dynamicFlow: Flow<PagingData<Dynamic>> = baseDynamicFlow.combine(likeStates) { pagingData, likes ->
        pagingData.map { dynamic ->
            val likeState = likes[dynamic.id]
            if (likeState != null) {
                val updatedStats = dynamic.modules.statsModule.copy(
                    like = dynamic.modules.statsModule.like.copy(
                        status = likeState
                    )
                )
                val updatedModules = dynamic.modules.copy(statsModule = updatedStats)
                dynamic.copy(modules = updatedModules)
            } else {
                dynamic
            }
        }
    }
    
    fun likeDynamic(dynamicId: String, isLiked: Boolean) {
        val newState = !isLiked
        likeStates.value = likeStates.value + (dynamicId to newState)
        
        viewModelScope.launch {
            repository.likeDynamic(dynamicId, if (isLiked) 0 else 1)
        }
    }
}
