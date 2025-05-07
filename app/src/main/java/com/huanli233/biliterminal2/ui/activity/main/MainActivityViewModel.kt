package com.huanli233.biliterminal2.ui.activity.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.data.account.AccountManager
import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliterminal2.data.proto.AppSettings
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliwebapi.api.interfaces.ICookieApi
import com.huanli233.biliwebapi.api.interfaces.IMainPage
import com.huanli233.biliwebapi.api.util.CookieRefreshUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InitializationState {
    object Loading : InitializationState()
    object Success : InitializationState()
    data class Error(val message: String) : InitializationState()
    object NavigateToSetup : InitializationState()
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _initializationState = MutableLiveData<InitializationState>(InitializationState.Loading)
    val initializationState: LiveData<InitializationState> get() = _initializationState

    init {
        initializeAppData()
    }

    fun initializeAppData() {
        _initializationState.value = InitializationState.Loading

        viewModelScope.launch {
            if (DataStore.appSettings.firstRun) {
                _initializationState.value = InitializationState.NavigateToSetup
                return@launch
            }

            val needCheckRefresh = (System.currentTimeMillis() - DataStore.appSettings.lastCheckCookieRefresh) > 1000 * 60 * 60 * 24

            if (AccountManager.loggedIn() && needCheckRefresh) {
                runCatching {
                    bilibiliApi.api(ICookieApi::class) {
                        cookieInfo()
                    }.apiResultNonNull()
                }.onSuccess { cookieInfoResult ->
                    cookieInfoResult.onSuccess {
                        if (it.refresh) {
                            val correspondPath = CookieRefreshUtil.getCorrespondPath(it.timestamp)
                            runCatching {
                                bilibiliApi.api<ICookieApi>().requestCorrespondPath(correspondPath)
                            }.onSuccess { correspondPathResult ->
                                val refreshCsrf = CookieRefreshUtil.extractRefreshCsrf(correspondPathResult)
                                runCatching {
                                    bilibiliApi.api(ICookieApi::class) {
                                        refreshCookie(refreshCsrf.orEmpty(), AccountManager.currentAccount.refreshToken.orEmpty())
                                    }.apiResultNonNull()
                                }.onSuccess { refreshCookieResult ->
                                    refreshCookieResult.onSuccess {
                                        DataStore.editData {
                                            lastCheckCookieRefresh = System.currentTimeMillis()
                                        }
                                        accountRepository.updateAccount(AccountManager.currentAccount.copy(refreshToken = it.refreshToken))
                                        _initializationState.value = InitializationState.Success
                                    }.onFailure { error ->
                                        _initializationState.value = InitializationState.Error(error.message ?: "Unknown refresh error")
                                    }
                                }.onFailure { error ->
                                    _initializationState.value = InitializationState.Error(error.message ?: "Unknown correspond path error")
                                }
                            }.onFailure { error ->
                                _initializationState.value = InitializationState.Error(error.message ?: "Unknown cookie info error")
                            }
                        } else {
                            DataStore.editData {
                                lastCheckCookieRefresh = System.currentTimeMillis()
                            }
                            _initializationState.value = InitializationState.Success
                        }
                    }.onFailure { error ->
                        _initializationState.value = InitializationState.Error(error.message ?: "Unknown cookie info error")
                    }
                }.onFailure { error ->
                    _initializationState.value = InitializationState.Error(error.message ?: "Network or API error")
                }
            } else {
                _initializationState.value = InitializationState.Success
            }
        }
    }

    fun retryInitialization() {
        initializeAppData()
    }
}