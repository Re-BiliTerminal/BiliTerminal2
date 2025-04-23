package com.huanli233.biliterminal2.activity.settings.login

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.util.QRCodeUtil
import com.huanli233.biliwebapi.bean.login.QrCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

enum class QRLoginState {
    NONE,
    WAITING,
    EXPIRED,
    SCANNED,
    LOGGED_IN,
    ERROR_NETWORK,
    ERROR_API
}

data class QRLoginUiState(
    val qrBitmap: Bitmap? = null,
    val state: QRLoginState = QRLoginState.NONE,
    val isRefreshEnabled: Boolean = false,
    val qrCode: QrCode? = null
)

class QRLoginViewModel : ViewModel() {
    private val _uiState = MutableLiveData(QRLoginUiState())
    val uiState: LiveData<QRLoginUiState> = _uiState
    
    private var pollingJob: Job? = null

    fun refreshQrCode() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value?.copy(state = QRLoginState.NONE)
                val qrCode = QrCode.generate(bilibiliApi)
                val qrBitmap = QRCodeUtil.createQRCodeBitmap(qrCode.data!!.url, 320, 320)
                
                _uiState.value = QRLoginUiState(
                    qrBitmap = qrBitmap,
                    state = QRLoginState.WAITING,
                    isRefreshEnabled = false,
                    qrCode = qrCode.data
                )
                
                startPolling(qrCode.data!!)
            } catch (e: IOException) {
                _uiState.value = _uiState.value?.copy(
                    state = QRLoginState.ERROR_NETWORK,
                    isRefreshEnabled = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    state = QRLoginState.ERROR_API,
                    isRefreshEnabled = true
                )
            }
        }
    }

    private fun startPolling(qrCode: QrCode) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = qrCode.poll()
                    val currentState = when (response.data?.code) {
                        86038 -> QRLoginState.EXPIRED
                        86090 -> QRLoginState.SCANNED
                        0 -> QRLoginState.LOGGED_IN
                        else -> QRLoginState.WAITING
                    }
                    
                    _uiState.value = _uiState.value?.copy(
                        state = currentState,
                        isRefreshEnabled = currentState == QRLoginState.EXPIRED
                    )

                    if (currentState == QRLoginState.EXPIRED || currentState == QRLoginState.LOGGED_IN) {
                        break
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value?.copy(
                        state = QRLoginState.ERROR_NETWORK,
                        isRefreshEnabled = true
                    )
                    break
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
