package com.huanli233.bilizepam.ui.screens.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.huanli233.bilizepam.data.repository.ReplyRepository
import com.huanli233.biliwebapi.bean.reply.Reply
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentDetailUiState(
    val isLikingReply: Boolean = false,
    val likedReplies: Set<Long> = emptySet(),
    val replyLikeCounts: Map<Long, Int> = emptyMap(),
    val topReplyIds: Set<Long> = emptySet()
)

sealed class CommentDetailEvent {
    data class LikeSuccess(val replyId: Long, val isLiked: Boolean) : CommentDetailEvent()
    data class LikeFailed(val message: String) : CommentDetailEvent()
    data class LoginRequired(val message: String) : CommentDetailEvent()
}

@HiltViewModel
class CommentDetailViewModel @Inject constructor(
    private val replyRepository: ReplyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentDetailUiState())
    val uiState: StateFlow<CommentDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<CommentDetailEvent?>(null)
    val events: Flow<CommentDetailEvent> = _events.asStateFlow().filterNotNull()

    private var currentOid: Long = 0
    private var currentReplyId: Long = 0
    private var currentType: Int = 1

    private var _commentsPager: Flow<PagingData<Reply>>? = null
    val comments: Flow<PagingData<Reply>>
        get() = _commentsPager ?: throw IllegalStateException("Comments not initialized. Call setReplyDetail first.")

    fun setReplyDetail(replyId: Long, oid: Long, type: Int = 1) {
        if (currentReplyId != replyId || currentOid != oid || currentType != type) {
            currentReplyId = replyId
            currentOid = oid
            currentType = type
            
            _commentsPager = Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = { 
                    CommentDetailPagingSource(
                        replyRepository = replyRepository,
                        oid = oid,
                        rootReplyId = replyId,
                        type = type
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }
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
                    _events.value = CommentDetailEvent.LikeSuccess(replyId, !isCurrentlyLiked)
                }.onFailure { error ->
                    if (error.message?.contains("登录") == true) {
                        _events.value = CommentDetailEvent.LoginRequired("请先登录后再点赞")
                    } else {
                        _events.value = CommentDetailEvent.LikeFailed(error.message ?: "点赞失败")
                    }
                }
            } catch (e: Exception) {
                _events.value = CommentDetailEvent.LikeFailed("网络错误")
            } finally {
                _uiState.value = _uiState.value.copy(isLikingReply = false)
            }
        }
    }
}
