package com.huanli233.biliterminal2.ui.fragment.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.data.account.Account
import com.huanli233.biliterminal2.data.account.AccountDao
import com.huanli233.biliterminal2.data.account.AccountRepository
import com.huanli233.biliterminal2.data.account.toEntity
import com.huanli233.biliterminal2.utils.extensions.LoadState
import com.huanli233.biliwebapi.api.interfaces.ILoginApi
import com.huanli233.biliwebapi.bean.login.QrCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QrCodeLoginState(
    val code: Int = -1,
    val finished: Boolean = false
)

@HiltViewModel
class QrCodeLoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountDao: AccountDao
): ViewModel() {

    private var _qrcodeState: MutableLiveData<LoadState<String>> = MutableLiveData()
    val qrcodeState: LiveData<LoadState<String>>
        get() = _qrcodeState

    private var _qrCodeLoginState: MutableLiveData<LoadState<QrCodeLoginState>> = MutableLiveData()
    val qrCodeLoginState: LiveData<LoadState<QrCodeLoginState>>
        get() = _qrCodeLoginState

    private var _needRefresh: MutableLiveData<Boolean> = MutableLiveData()
    val needRefresh: LiveData<Boolean>
        get() = _needRefresh

    private var pollJob: Job? = null
    private var qrcodeKey: String? = null

    init {
        loadQrcode()
    }

    fun loadQrcode() {
        viewModelScope.launch {
            _qrcodeState.postValue(LoadState.Loading())
            bilibiliApi.api(ILoginApi::class) {
                requestQrCode()
            }.apiResultNonNull().onSuccess {
                qrcodeKey = it.qrcodeKey
                _needRefresh.postValue(false)
                _qrcodeState.postValue(LoadState.Success(it.url))
                startPoll()
            }.onFailure {
                _qrcodeState.postValue(LoadState.Error(it))
                _needRefresh.postValue(true)
            }
        }
    }

    fun startPoll() {
        stopPoll()
        pollJob = viewModelScope.launch {
            while (isActive) {
                bilibiliApi.api(ILoginApi::class) {
                    qrCodeLogin(qrcodeKey.orEmpty())
                }.apiResultNonNull().onSuccess {
                    _qrCodeLoginState.postValue(LoadState.Success(QrCodeLoginState(it.code)))
                    if (it.code == 0) {
                        stopPoll()
                        login(it)
                    } else if (it.code != 86090 && it.code != 86101) {
                        _needRefresh.postValue(true)
                        stopPoll()
                    }
                }.onFailure {
                    _needRefresh.postValue(true)
                    _qrCodeLoginState.postValue(LoadState.Error(it))
                }
                delay(1000)
            }
        }
    }

    fun stopPoll() {
        pollJob?.cancel()
    }

    fun login(loginResult: QrCode.LoginResult) {
        viewModelScope.launch {
            accountDao.insertAccount(
                Account(UserPreferences.activeAccountId.get(), null, null, System.currentTimeMillis()).toEntity()
            )
            accountRepository.updateActiveAccountToken {
                it.copy(refreshToken = loginResult.refreshToken)
            }
            _qrCodeLoginState.postValue(LoadState.Success(QrCodeLoginState(0, true)))
        }
    }

}