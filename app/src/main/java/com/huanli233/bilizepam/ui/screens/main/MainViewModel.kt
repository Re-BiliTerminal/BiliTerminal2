package com.huanli233.bilizepam.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.data.setting.edit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiState {
    object Loading : MainUiState
    object NeedsSetup : MainUiState
    object NeedsLogin : MainUiState
    object Ready : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val uiState: StateFlow<MainUiState> = LocalData.settingsStateFlow
        .map { settings ->
            when {
                settings == null -> MainUiState.Loading

                settings.firstRun -> MainUiState.NeedsSetup

                else -> MainUiState.Ready
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState.Loading
        )

    fun onSetupComplete() {
        viewModelScope.launch {
            LocalData.edit {
                firstRun = false
            }
        }
    }
}