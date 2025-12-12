package com.huanli233.bilizepam.ui.screens.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.account.AccountRepository
import com.huanli233.bilizepam.data.repository.ReplyRepository
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.biliwebapi.bean.reply.RepliesControl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CommentSortMode(val value: Int, val label: String) {
    BY_HOT(3, "按热度"),
    BY_TIME(2, "按时间"),
    BY_HOT_AND_TIME(1, "热度+时间")
}

data class CommentUiState(
    val isLikingReply: Boolean = false,
    val likedReplies: Set<Long> = emptySet(),
    val replyLikeCounts: Map<Long, Int> = emptyMap(),
    val topReplyIds: Set<Long> = emptySet(),
    val control: RepliesControl? = null,
    val sortMode: CommentSortMode = CommentSortMode.BY_TIME
)

sealed class CommentEvent {
    data class LikeSuccess(val replyId: Long, val isLiked: Boolean) : CommentEvent()
    data class LikeFailed(val message: String) : CommentEvent()
    data class LoginRequired(val message: String) : CommentEvent()
}

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val replyRepository: ReplyRepository,
    val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    private val _events = Channel<CommentEvent>(Channel.BUFFERED)
    val events: Flow<CommentEvent> = _events.receiveAsFlow()

    private var currentOid: Long = 0
    private var currentType: Int = 1

    private var _commentsPager: Flow<PagingData<Reply>>? = null
    val comments: Flow<PagingData<Reply>>
        get() = _commentsPager ?: throw IllegalStateException("Comments not initialized. Call setOid first.")

    fun setOid(oid: Long, type: Int = 1) {
        if (currentOid != oid || currentType != type) {
            currentOid = oid
            currentType = type
            refreshComments()
        }
    }
    
    fun setSortMode(mode: CommentSortMode) {
        if (_uiState.value.sortMode != mode) {
            _uiState.value = _uiState.value.copy(sortMode = mode)
            refreshComments()
        }
    }
    
    private fun refreshComments() {
        _commentsPager = Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { 
                CommentPagingSource(
                    replyRepository = replyRepository,
                    oid = currentOid,
                    type = currentType,
                    mode = _uiState.value.sortMode.value,
                    onTopRepliesLoaded = { topReplyIds ->
                        updateTopReplyIds(topReplyIds)
                    },
                    onControlLoaded = { control ->
                        _uiState.value = _uiState.value.copy(control = control)
                    }
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    // 保持向后兼容
    fun setAid(aid: Long) {
        setOid(aid, 1)
    }

    fun likeReply(replyId: Long, isCurrentlyLiked: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLikingReply = true)
            
            try {
                val action = if (isCurrentlyLiked) 0 else 1
                val result = replyRepository.likeReply(
                    oid = currentOid,
                    replyId = replyId,
                    action = action
                )
                
                result.onSuccess {
                    // 更新本地点赞状态和数量
                    val currentLikedReplies = _uiState.value.likedReplies.toMutableSet()
                    val currentLikeCounts = _uiState.value.replyLikeCounts.toMutableMap()
                    
                    if (isCurrentlyLiked) {
                        currentLikedReplies.remove(replyId)
                        // 减少点赞数，如果没有记录则不改变
                        currentLikeCounts[replyId]?.let { count ->
                            currentLikeCounts[replyId] = maxOf(0, count - 1)
                        }
                    } else {
                        currentLikedReplies.add(replyId)
                        // 增加点赞数，如果没有记录则不改变
                        currentLikeCounts[replyId]?.let { count ->
                            currentLikeCounts[replyId] = count + 1
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        likedReplies = currentLikedReplies,
                        replyLikeCounts = currentLikeCounts
                    )
                    _events.send(CommentEvent.LikeSuccess(replyId, !isCurrentlyLiked))
                }.onFailure { error ->
                    if (error.message?.contains("登录") == true) {
                        _events.send(CommentEvent.LoginRequired("请先登录后再点赞"))
                    } else {
                        _events.send(CommentEvent.LikeFailed(error.message ?: "点赞失败"))
                    }
                }
            } catch (e: Exception) {
                _events.send(CommentEvent.LikeFailed("网络错误"))
            } finally {
                _uiState.value = _uiState.value.copy(isLikingReply = false)
            }
        }
    }

    fun isTopReply(replyId: Long): Boolean {
        return _uiState.value.topReplyIds.contains(replyId)
    }

    fun updateTopReplyIds(topReplyIds: Set<Long>) {
        _uiState.value = _uiState.value.copy(topReplyIds = topReplyIds)
    }
}
