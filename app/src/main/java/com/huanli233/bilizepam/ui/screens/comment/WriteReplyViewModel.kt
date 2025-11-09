package com.huanli233.bilizepam.ui.screens.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.repository.ReplyRepository
import com.huanli233.bilizepam.utils.MsgUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WriteReplyUiState(
    val isSending: Boolean = false,
    val error: String? = null
)

sealed class WriteReplyEvent {
    data object Success : WriteReplyEvent()
    data class Error(val message: String) : WriteReplyEvent()
}

@HiltViewModel
class WriteReplyViewModel @Inject constructor(
    private val replyRepository: ReplyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteReplyUiState())
    val uiState: StateFlow<WriteReplyUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WriteReplyEvent>()
    val events: SharedFlow<WriteReplyEvent> = _events.asSharedFlow()

    private val errorMessages = mapOf(
        -101 to "没有登录或登录信息有误",
        -102 to "账号被封禁",
        -509 to "请求过于频繁",
        12015 to "需要评论验证码",
        12016 to "包含敏感内容",
        12025 to "字数过多",
        12035 to "被拉黑了",
        12051 to "重复评论，请勿刷屏"
    )

    fun sendReply(oid: Long, rpid: Long, parent: Long, message: String) {
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                error = null
            )

            try {
                val result = replyRepository.sendReply(oid, rpid, parent, message)
                
                result.onSuccess { reply ->
                    _uiState.value = _uiState.value.copy(isSending = false)
                    MsgUtil.showMsg("发送成功")
                    _events.emit(WriteReplyEvent.Success)
                }.onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("登录") == true -> "请先登录"
                        error.message?.contains("频繁") == true -> "请求过于频繁，请稍后再试"
                        error.message?.contains("敏感") == true -> "包含敏感内容，请修改后重试"
                        error.message?.contains("字数") == true -> "评论字数过多"
                        error.message?.contains("重复") == true -> "重复评论，请勿刷屏"
                        else -> error.message ?: "发送失败，请重试"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = errorMessage
                    )
                    MsgUtil.showMsg("评论发送失败：$errorMessage")
                    _events.emit(WriteReplyEvent.Error(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = "网络错误，请检查网络连接"
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = errorMessage
                )
                MsgUtil.showMsg(errorMessage)
                _events.emit(WriteReplyEvent.Error(errorMessage))
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
