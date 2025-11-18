package com.huanli233.bilizepam.ui.screens.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.UserRepository
import com.huanli233.biliwebapi.bean.user.NavUserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MySpaceUiState {
    data object Loading : MySpaceUiState()
    data class Success(val navUserInfo: NavUserInfo) : MySpaceUiState()
    data class Error(val message: String) : MySpaceUiState()
}

@HiltViewModel
class MySpaceViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MySpaceUiState>(MySpaceUiState.Loading)
    val uiState: StateFlow<MySpaceUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = MySpaceUiState.Loading
            
            userRepository.getMyInfo().fold(
                onSuccess = { navUserInfo ->
                    _uiState.value = MySpaceUiState.Success(navUserInfo)
                },
                onFailure = { error ->
                    _uiState.value = MySpaceUiState.Error(
                        error.message ?: "Failed to load user info"
                    )
                }
            )
        }
    }
}
