package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.OpusRepository
import com.huanli233.biliwebapi.bean.opus.Opus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpusDetailViewModel @Inject constructor(
    private val opusRepository: OpusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OpusDetailUiState>(OpusDetailUiState.Loading)
    val uiState: StateFlow<OpusDetailUiState> = _uiState.asStateFlow()
    
    private val _events = Channel<OpusDetailEvent>()
    val events = _events.receiveAsFlow()
    
    private var currentOpusId: String = ""

    fun loadOpus(opusId: String) {
        currentOpusId = opusId
        viewModelScope.launch {
            _uiState.value = OpusDetailUiState.Loading
            opusRepository.getOpusDetail(opusId).fold(
                onSuccess = { result ->
                    if (result.fallback?.id != null) {
                        _uiState.value = OpusDetailUiState.Error(
                            "此内容为旧版专栏格式，暂不支持查看"
                        )
                    } else {
                        _uiState.value = OpusDetailUiState.Success(result.item)
                    }
                },
                onFailure = { error ->
                    _uiState.value = OpusDetailUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }
    
    fun like() {
        val currentState = _uiState.value
        if (currentState !is OpusDetailUiState.Success) return
        
        viewModelScope.launch {
            val isCurrentlyLiked = currentState.opus.modules.moduleStat.like.status
            val newStatus = !isCurrentlyLiked
            
            opusRepository.likeOpus(currentOpusId, newStatus).fold(
                onSuccess = {
                    _uiState.update { state ->
                        if (state is OpusDetailUiState.Success) {
                            val updatedStat = state.opus.modules.moduleStat.copy(
                                like = state.opus.modules.moduleStat.like.copy(
                                    status = newStatus,
                                    count = if (newStatus) 
                                        state.opus.modules.moduleStat.like.count + 1 
                                    else 
                                        state.opus.modules.moduleStat.like.count - 1
                                )
                            )
                            val updatedModules = state.opus.modules.copy(moduleStat = updatedStat)
                            val updatedOpus = state.opus.copy(modules = updatedModules)
                            OpusDetailUiState.Success(updatedOpus)
                        } else {
                            state
                        }
                    }
                    _events.send(OpusDetailEvent.LikeSuccess(newStatus))
                },
                onFailure = { error ->
                    if (error.message?.contains("未登录") == true) {
                        _events.send(OpusDetailEvent.NotLoggedIn)
                    } else {
                        _events.send(OpusDetailEvent.OperationFailed(error.message))
                    }
                }
            )
        }
    }
    
    fun favorite() {
        val currentState = _uiState.value
        if (currentState !is OpusDetailUiState.Success) return
        
        viewModelScope.launch {
            val isCurrentlyFavorited = currentState.opus.modules.moduleStat.favorite?.status ?: false
            val newStatus = !isCurrentlyFavorited
            
            opusRepository.favoriteOpus(currentOpusId, newStatus).fold(
                onSuccess = {
                    _uiState.update { state ->
                        if (state is OpusDetailUiState.Success) {
                            val currentFav = state.opus.modules.moduleStat.favorite
                            val updatedFav = currentFav?.copy(
                                status = newStatus,
                                count = if (newStatus) currentFav.count + 1 else currentFav.count - 1
                            )
                                ?: com.huanli233.biliwebapi.bean.opus.OpusStatModule.OpusStat(
                                    count = if (newStatus) 1 else 0,
                                    forbidden = false,
                                    status = newStatus,
                                    hidden = false
                                )
                            val updatedStat = state.opus.modules.moduleStat.copy(favorite = updatedFav)
                            val updatedModules = state.opus.modules.copy(moduleStat = updatedStat)
                            val updatedOpus = state.opus.copy(modules = updatedModules)
                            OpusDetailUiState.Success(updatedOpus)
                        } else {
                            state
                        }
                    }
                    _events.send(OpusDetailEvent.FavoriteSuccess(newStatus))
                },
                onFailure = { error ->
                    if (error.message?.contains("未登录") == true) {
                        _events.send(OpusDetailEvent.NotLoggedIn)
                    } else {
                        _events.send(OpusDetailEvent.OperationFailed(error.message))
                    }
                }
            )
        }
    }
}

sealed class OpusDetailUiState {
    data object Loading : OpusDetailUiState()
    data class Success(val opus: Opus) : OpusDetailUiState()
    data class Error(val message: String) : OpusDetailUiState()
}

sealed class OpusDetailEvent {
    data class LikeSuccess(val isLiked: Boolean) : OpusDetailEvent()
    data class FavoriteSuccess(val isFavorited: Boolean) : OpusDetailEvent()
    data object NotLoggedIn : OpusDetailEvent()
    data class OperationFailed(val message: String?) : OpusDetailEvent()
}
