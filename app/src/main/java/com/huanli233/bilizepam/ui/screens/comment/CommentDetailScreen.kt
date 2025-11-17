package com.huanli233.bilizepam.ui.screens.comment


import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.plus
import androidx.wear.compose.material3.Text
import coil3.compose.AsyncImage
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.EmoteText
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.components.RichText
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.utils.ArticleRedirectUtil
import com.huanli233.bilizepam.utils.MsgUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentDetailScreen(
    replyId: Long,
    oid: Long,
    type: Int = 1,
    onBackClick: () -> Unit = {},
    onWriteReplyClick: (Long, Long, Long, String?) -> Unit = { _, _, _, _ -> },
    onUserClick: (Long) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    onVideoClick: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: CommentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRound = isRoundDevice() && LocalData.settings.uiSettings.roundMode
    val scrollState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(replyId, oid, type) {
        viewModel.setReplyDetail(replyId, oid, type)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CommentDetailEvent.LikeSuccess -> {
                    val message = if (event.isLiked) "点赞成功" else "取消点赞"
                    MsgUtil.showMsg(message)
                }

                is CommentDetailEvent.LikeFailed -> {
                    MsgUtil.showMsg(event.message)
                }

                is CommentDetailEvent.LoginRequired -> {
                    MsgUtil.showMsg(event.message)
                }
            }
        }
    }

    // 确保ViewModel已初始化后再获取comments
    val comments = remember(replyId, oid, type) {
        viewModel.setReplyDetail(replyId, oid, type)
        viewModel.comments
    }.collectAsLazyPagingItems()
    
    // Create ScrollBehavior for TopBar
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        modifier = modifier,
        topBar = scrollAwareTopBar(
            title = "评论详情",
            onBackClick = onBackClick,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
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
                state = scrollState,
                contentPadding = paddingValues + PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 页面标题
                item {
                    DetailHeader(
                        childCommentCount = maxOf(0, comments.itemCount - 1) // 减去主评论
                    )
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
                        if (index == 0) {
                            // 第一个是根评论，使用特殊样式
                            RootCommentCard(
                                reply = reply,
                                isLiked = uiState.likedReplies.contains(reply.replyId) || (reply.actionState == 1),
                                onLikeClick = { replyItem, isLiked ->
                                    viewModel.likeReply(replyItem.replyId, isLiked)
                                },
                                onReplyClick = { replyItem ->
                                    onWriteReplyClick(
                                        replyItem.oid,
                                        replyItem.replyId,
                                        replyItem.replyId,
                                        replyItem.member.name
                                    )
                                },
                                onUserClick = onUserClick,
                                onOpusClick = onOpusClick,
                                uiState = uiState
                            )
                        } else {
                            // 子评论
                            ChildCommentCard(
                                reply = reply,
                                isLiked = uiState.likedReplies.contains(reply.replyId) || (reply.actionState == 1),
                                onLikeClick = { replyItem, isLiked ->
                                    viewModel.likeReply(replyItem.replyId, isLiked)
                                },
                                onReplyClick = {  replyItem ->
                                    onWriteReplyClick(
                                        reply.oid,
                                        reply.replyId,
                                        replyItem.replyId,
                                        replyItem.member.name
                                    )
                                },
                                onUserClick = onUserClick,
                                onOpusClick = onOpusClick,
                                uiState = uiState
                            )
                        }
                    }
                }

                // 底部加载更多状态
                item {
                    when (comments.loadState.append) {
                        is LoadState.Loading -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "加载更多回复...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
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

                // 空状态提示
                if (comments.itemCount <= 1) { // 只有根评论或没有评论，表示没有子评论
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Comment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "暂无回复",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "成为第一个回复的人吧",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun DetailHeader(
    childCommentCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Comment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "评论详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (childCommentCount > 0) "$childCommentCount 条回复" else "暂无回复",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            FilledTonalButton(
                onClick = onRetry,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun RootCommentCard(
    reply: Reply,
    isLiked: Boolean,
    onLikeClick: (Reply, Boolean) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onUserClick: (Long) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    uiState: CommentDetailUiState,
    modifier: Modifier = Modifier
) {
    val originalLiked = reply.actionState == 1
    val likeCountDelta = when {
        isLiked && !originalLiked -> 1
        !isLiked && originalLiked -> -1
        else -> 0
    }

    val modifiedReply = reply.copy(
        actionState = if (isLiked) 1 else 0,
        like = reply.like + likeCountDelta
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主评论内容（不显示子评论）
            CommentContent(
                reply = modifiedReply,
                onLikeClick = onLikeClick,
                onUserClick = onUserClick,
                onOpusClick = onOpusClick
            )

            // 分隔线
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // 回复按钮
            FilledTonalButton(
                onClick = { onReplyClick(reply) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Reply,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "回复评论",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CommentContent(
    reply: Reply,
    onLikeClick: (Reply, Boolean) -> Unit,
    onUserClick: (Long) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    onVideoClick: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val isLiked = reply.actionState == 1

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 头像（可点击）
        AsyncImage(
            model = reply.member.face?.takeIf { it.isNotBlank() },
            contentDescription = reply.member.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onUserClick(reply.member.mid) },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 用户信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reply.member.name ?: "未知用户",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = try {
                            java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(reply.createTime * 1000))
                        } catch (e: Exception) {
                            ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 点赞按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onLikeClick(reply, isLiked) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "点赞",
                        modifier = Modifier.size(16.dp),
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reply.like > 0) {
                        Text(
                            text = reply.like.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var showUrlDialog by remember { mutableStateOf<String?>(null) }
            var isLoadingArticle by remember { mutableStateOf(false) }
            
            // 评论内容
            RichText(
                text = reply.content.message ?: "",
                emotes = reply.content.emote,
                atList = reply.content.atNameToMid.toList(),
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = onUserClick,
                onBvidClick = { bvid -> onVideoClick(0L, bvid) },
                onAvidClick = { aid -> onVideoClick(aid, "") },
                onCvidClick = { cvid ->
                    isLoadingArticle = true
                    scope.launch {
                        ArticleRedirectUtil.convertCvidToOpusId(cvid).fold(
                            onSuccess = { opusId ->
                                isLoadingArticle = false
                                onOpusClick(opusId)
                            },
                            onFailure = { error ->
                                isLoadingArticle = false
                                Toast.makeText(context, "无法打开文章: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                onUrlClick = { url -> showUrlDialog = url }
            )
            
            if (isLoadingArticle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            
            showUrlDialog?.let { url ->
                AdaptDialog(
                    onDismissRequest = { showUrlDialog = null },
                    title = { Text("打开链接") },
                    text = { Text(url) },
                    confirmButton = { close ->
                        TextButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                            }
                            close()
                        }) {
                            Text("打开")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUrlDialog = null }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChildCommentCard(
    reply: Reply,
    isLiked: Boolean,
    onLikeClick: (Reply, Boolean) -> Unit,
    onReplyClick: (Reply) -> Unit,
    onUserClick: (Long) -> Unit = {},
    onOpusClick: (Long) -> Unit = {},
    onVideoClick: (Long, String) -> Unit = { _, _ -> },
    uiState: CommentDetailUiState,
    modifier: Modifier = Modifier
) {
    val originalLiked = reply.actionState == 1
    val likeCountDelta = when {
        isLiked && !originalLiked -> 1
        !isLiked && originalLiked -> -1
        else -> 0
    }

    val modifiedReply = reply.copy(
        actionState = if (isLiked) 1 else 0,
        like = reply.like + likeCountDelta
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 子评论内容
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 头像（可点击）
                AsyncImage(
                    model = reply.member.face?.takeIf { it.isNotBlank() },
                    contentDescription = reply.member.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onUserClick(reply.member.mid) },
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 用户信息行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reply.member.name ?: "未知用户",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = try {
                                    java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(reply.createTime * 1000))
                                } catch (e: Exception) {
                                    ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 点赞按钮
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLikeClick(reply, isLiked) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "点赞",
                                modifier = Modifier.size(14.dp),
                                tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (reply.like > 0) {
                                Text(
                                    text = reply.like.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    var showUrlDialog by remember { mutableStateOf<String?>(null) }
                    var isLoadingArticle by remember { mutableStateOf(false) }
                    
                    // 评论内容
                    RichText(
                        text = reply.content.message ?: "",
                        emotes = reply.content.emote,
                        atList = reply.content.atNameToMid.toList(),
                        style = MaterialTheme.typography.bodyMedium,
                        onUserClick = onUserClick,
                        onBvidClick = { bvid -> onVideoClick(0L, bvid) },
                        onAvidClick = { aid -> onVideoClick(aid, "") },
                        onCvidClick = { cvid ->
                            isLoadingArticle = true
                            scope.launch {
                                ArticleRedirectUtil.convertCvidToOpusId(cvid).fold(
                                    onSuccess = { opusId ->
                                        isLoadingArticle = false
                                        onOpusClick(opusId)
                                    },
                                    onFailure = { error ->
                                        isLoadingArticle = false
                                        Toast.makeText(context, "无法打开文章: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onUrlClick = { url -> showUrlDialog = url }
                    )
                    
                    if (isLoadingArticle) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    
                    showUrlDialog?.let { url ->
                        AdaptDialog(
                            onDismissRequest = { showUrlDialog = null },
                            title = { Text("打开链接") },
                            text = { Text(url) },
                            confirmButton = { close ->
                                TextButton(onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                                    }
                                    close()
                                }) {
                                    Text("打开")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showUrlDialog = null }) {
                                    Text("取消")
                                }
                            }
                        )
                    }
                }
            }

            // 回复按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onReplyClick(reply) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Reply,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "回复",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

