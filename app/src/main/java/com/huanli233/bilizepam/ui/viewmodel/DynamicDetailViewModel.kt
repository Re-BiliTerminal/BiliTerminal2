package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.DynamicRepository
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import com.huanli233.biliwebapi.bean.opus.OpusStatModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DynamicDetailViewModel @Inject constructor(
    private val dynamicRepository: DynamicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DynamicDetailUiState>(DynamicDetailUiState.Loading)
    val uiState: StateFlow<DynamicDetailUiState> = _uiState.asStateFlow()

    fun loadDynamic(dynamicId: String) {
        viewModelScope.launch {
            _uiState.value = DynamicDetailUiState.Loading
            dynamicRepository.getDynamicDetail(dynamicId).fold(
                onSuccess = { dynamic ->
                    _uiState.value = DynamicDetailUiState.Success(dynamic)
                },
                onFailure = { error ->
                    _uiState.value = DynamicDetailUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun likeDynamic(dynamicId: String, isLiked: Boolean) {
        viewModelScope.launch {
            dynamicRepository.likeDynamic(dynamicId, if (isLiked) 0 else 1).fold(
                onSuccess = {
                    if (_uiState.value is DynamicDetailUiState.Success) {
                        val currentDynamic = (_uiState.value as DynamicDetailUiState.Success).dynamic
                        val updatedStats = currentDynamic.modules.statsModule.copy(
                            like = currentDynamic.modules.statsModule.like.copy(
                                status = !isLiked
                            )
                        )
                        val updatedModules = currentDynamic.modules.copy(statsModule = updatedStats)
                        val updatedDynamic = currentDynamic.copy(modules = updatedModules)
                        _uiState.value = DynamicDetailUiState.Success(updatedDynamic)
                    }
                },
                onFailure = {
                }
            )
        }
    }
}

sealed class DynamicDetailUiState {
    data object Loading : DynamicDetailUiState()
    data class Success(val dynamic: Dynamic) : DynamicDetailUiState()
    data class Error(val message: String) : DynamicDetailUiState()
}
