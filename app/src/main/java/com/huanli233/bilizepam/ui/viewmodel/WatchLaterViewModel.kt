package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.WatchLaterRepository
import com.huanli233.biliwebapi.bean.watchlater.WatchLaterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WatchLaterUiState {
    data object Loading : WatchLaterUiState()
    data class Success(val items: List<WatchLaterItem>) : WatchLaterUiState()
    data class Error(val message: String) : WatchLaterUiState()
}

@HiltViewModel
class WatchLaterViewModel @Inject constructor(
    private val watchLaterRepository: WatchLaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchLaterUiState>(WatchLaterUiState.Loading)
    val uiState: StateFlow<WatchLaterUiState> = _uiState.asStateFlow()

    init {
        loadWatchLaterList()
    }

    fun loadWatchLaterList() {
        viewModelScope.launch {
            _uiState.value = WatchLaterUiState.Loading
            
            watchLaterRepository.getWatchLaterList().fold(
                onSuccess = { response ->
                    _uiState.value = WatchLaterUiState.Success(response.list ?: emptyList())
                },
                onFailure = { error ->
                    _uiState.value = WatchLaterUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun deleteItem(aid: Long) {
        viewModelScope.launch {
            watchLaterRepository.deleteFromWatchLater(aid).fold(
                onSuccess = {
                    loadWatchLaterList()
                },
                onFailure = { error ->
                    _uiState.value = WatchLaterUiState.Error(
                        error.message ?: "Delete failed"
                    )
                }
            )
        }
    }
}
