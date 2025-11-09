package com.huanli233.bilizepam.ui.screens.comment

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.plus
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.biliwebapi.bean.reply.Reply
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    aid: Long,
    type: Int = 1,
    modifier: Modifier = Modifier,
    viewModel: CommentViewModel = hiltViewModel(),
    scrollState: ScalingLazyListState? = null,
    onLoginClick: () -> Unit = {},
    onCommentDetailClick: (Long) -> Unit = {},
    onWriteReplyClick: (Long, Long, Long, String?) -> Unit = { _, _, _, _ -> },
    onUserClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeAccount by viewModel.accountRepository.activeAccount.collectAsState()
    val context = LocalContext.current
    val internalScrollState = rememberScalingLazyListState()
    val actualScrollState = scrollState ?: internalScrollState
    val scope = rememberCoroutineScope()
    val isRound = isRoundDevice() && LocalData.settings.uiSettings.roundMode

    LaunchedEffect(aid, type) {
        viewModel.setOid(aid, type)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CommentEvent.LikeSuccess -> {
                    val message = if (event.isLiked) "点赞成功" else "取消点赞"
                    MsgUtil.showMsg(message)
                }
                is CommentEvent.LikeFailed -> {
                    MsgUtil.showMsg(event.message)
                }
                is CommentEvent.LoginRequired -> {
                    MsgUtil.showMsg(event.message)
                }
            }
        }
    }

    // 确保ViewModel已初始化后再获取comments
    val comments = remember(aid, type) {
        viewModel.setOid(aid, type)
        viewModel.comments
    }.collectAsLazyPagingItems()
    val isLoggedIn = activeAccount != null

    ScreenScaffold(
        scrollState = actualScrollState,
        modifier = modifier
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = comments.loadState.refresh is LoadState.Loading,
            onRefresh = {
                scope.launch {
                    comments.refresh()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = actualScrollState,
                contentPadding = paddingValues.plus(PaddingValues(
                    vertical = 16.dp
                ))
            ) {
                // 评论总数显示
                item {
                    if (comments.itemCount > 0) {
                        Text(
                            text = "评论 ${comments.itemCount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }

                // 加载状态处理
                item {
                    Crossfade(
                        targetState = comments.loadState.refresh,
                        animationSpec = tween(durationMillis = 300),
                        label = "LoadingStateTransition"
                    ) { state ->
                        when (state) {
                            is LoadState.Loading if comments.itemCount == 0 -> {
                                LoadingView(
                                    state = LoadingState.LOADING,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                            is LoadState.Error -> {
                                ErrorCard(
                                    message = state.error.message ?: "加载失败",
                                    onRetry = { comments.retry() }
                                )
                            }
                            else -> {
                                // 空状态或成功状态不显示任何内容
                            }
                        }
                    }
                }

                // 评论列表
                items(comments.itemCount) { index ->
                    val reply = comments[index]
                    if (reply != null) {
                        // 判断是否为置顶评论（通过检查是否在置顶评论列表中）
                        val isTopReply = viewModel.isTopReply(reply.replyId)
                        
                        CommentItemWithLikeState(
                            reply = reply,
                            isLiked = uiState.likedReplies.contains(reply.replyId) || (reply.actionState == 1),
                            onLikeClick = { replyItem, isLiked ->
                                viewModel.likeReply(replyItem.replyId, isLiked)
                            },
                            isRound = isRound,
                            isTopReply = isTopReply,
                            onCommentClick = { clickedReply ->
                                onCommentDetailClick(clickedReply.replyId)
                            },
                            onReplyClick = { replyToReply ->
                                onWriteReplyClick(
                                    replyToReply.oid,
                                    replyToReply.replyId,
                                    replyToReply.replyId,
                                    replyToReply.member.name
                                )
                            },
                            onUserClick = onUserClick,
                            uiState = uiState
                        )
                    }
                }

                // 底部加载更多状态
                item {
                    when (comments.loadState.append) {
                        is LoadState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                        is LoadState.Error -> {
                            ErrorCard(
                                message = "加载更多失败",
                                onRetry = { comments.retry() }
                            )
                        }
                        else -> {
                            // 成功状态不显示任何内容
                        }
                    }
                }

                // 未登录提醒
                if (!isLoggedIn && comments.itemCount >= 3) {
                    item {
                        LoginReminderCard(
                            onLoginClick = onLoginClick,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun LoginReminderCard(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Login,
                contentDescription = "登录",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "登录后查看更多评论",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "立即登录",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun CommentItemWithLikeState(
    reply: Reply,
    isLiked: Boolean,
    onLikeClick: (Reply, Boolean) -> Unit,
    isRound: Boolean,
    isTopReply: Boolean,
    onCommentClick: (Reply) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onUserClick: (Long) -> Unit = {},
    uiState: CommentUiState
) {
    // 创建一个修改后的Reply对象，更新点赞状态和数量
    val originalLiked = reply.actionState == 1
    val likeCountDelta = when {
        isLiked && !originalLiked -> 1  // 新点赞
        !isLiked && originalLiked -> -1 // 取消点赞
        else -> 0 // 无变化
    }
    
    val modifiedReply = reply.copy(
        actionState = if (isLiked) 1 else 0,
        like = reply.like + likeCountDelta
    )
    
    CommentItem(
        reply = modifiedReply,
        onLikeClick = onLikeClick,
        isRound = isRound,
        isTopReply = isTopReply,
        onCommentClick = onCommentClick,
        onReplyClick = onReplyClick,
        onUserClick = onUserClick,
        uiState = uiState
    )
}

