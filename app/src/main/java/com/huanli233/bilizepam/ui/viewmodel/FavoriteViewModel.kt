package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.repository.FavoriteRepository
import com.huanli233.biliwebapi.bean.favorite.FavoriteBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FavoriteUiState {
    data object Loading : FavoriteUiState()
    data class Success(val folders: List<FavoriteBox>) : FavoriteUiState()
    data class Error(val message: String) : FavoriteUiState()
}

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Loading)
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        loadFavoriteFolders()
    }

    fun loadFavoriteFolders() {
        viewModelScope.launch {
            _uiState.value = FavoriteUiState.Loading
            
            val mid = AccountManager.currentAccount.accountId
            favoriteRepository.getFavoriteBoxList(mid).fold(
                onSuccess = { response ->
                    _uiState.value = FavoriteUiState.Success(response.list ?: emptyList())
                },
                onFailure = { error ->
                    _uiState.value = FavoriteUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }
}
