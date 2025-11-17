package com.huanli233.bilizepam.ui.screens.comment

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.bilizepam.ui.components.RichText
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.utils.ArticleRedirectUtil
import com.huanli233.bilizepam.utils.extensions.formatNumber
import com.huanli233.bilizepam.utils.extensions.formatToRelativeTime

@Composable
fun CommentItem(
    reply: Reply,
    onLikeClick: (Reply, Boolean) -> Unit,
    isRound: Boolean,
    isTopReply: Boolean = false,
    onCommentClick: (Reply) -> Unit = {},
    onReplyClick: (Reply) -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onBvidClick: (String) -> Unit = {},
    onAvidClick: (Long) -> Unit = {},
    onCvidClick: (Long) -> Unit = {},
    onUrlClick: (String) -> Unit = {},
    uiState: CommentUiState? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isLiked = reply.actionState == 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onCommentClick(reply) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = reply.member.face?.takeIf { it.isNotBlank() }?.also {
                        Log.d("CommentAvatar", "Loading avatar: $it for user: ${reply.member.name}")
                    } ?: run {
                        Log.w("CommentAvatar", "Avatar URL is null or empty for user: ${reply.member.name}")
                        null
                    },
                    contentDescription = reply.member.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onUserClick(reply.member.mid) },
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        Log.d("CommentAvatar", "Avatar loaded successfully for: ${reply.member.name}")
                    },
                    onError = { error ->
                        Log.e("CommentAvatar", "Failed to load avatar for ${reply.member.name}: ${error.result.throwable}")
                    }
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = reply.member.name ?: "未知用户",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isTopReply) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PushPin,
                                        contentDescription = "置顶",
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "置顶",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = (reply.createTime * 1000).formatToRelativeTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    val maxLines = if (expanded) Int.MAX_VALUE else 3
                    val messageText = reply.content.message ?: ""
                    var isTextOverflowing by remember { mutableStateOf(false) }

                    Column {
                        RichText(
                            text = messageText,
                            emotes = reply.content.emote,
                            atList = reply.content.atNameToMid.toList(),
                            style = MaterialTheme.typography.bodyMedium,
                            onUserClick = onUserClick,
                            onBvidClick = onBvidClick,
                            onAvidClick = onAvidClick,
                            onCvidClick = onCvidClick,
                            onUrlClick = onUrlClick
                        )

                        if (isTextOverflowing || expanded) {
                            Text(
                                text = if (expanded) "收起" else "展开",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clickable {
                                        expanded = !expanded
                                    }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (reply.replies?.isNotEmpty() == true) {
                            Text(
                                text = "${reply.rcount} 条回复",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    // TODO: 展开子评论
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        text = reply.like.formatNumber("万", "亿"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // 回复按钮
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onReplyClick(reply) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Reply,
                                    contentDescription = "回复",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "回复",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            val childReplies = reply.replies
            if (!childReplies.isNullOrEmpty() && childReplies.size <= 3) {
                Spacer(modifier = Modifier.height(8.dp))
                childReplies.take(3).forEach { childReply ->
                    ChildCommentItemWithLikeState(
                        reply = childReply,
                        onLikeClick = onLikeClick,
                        onCommentClick = { onCommentClick(reply) }, // 点击子评论时传递主评论
                        uiState = uiState,
                        modifier = Modifier.padding(start = 40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChildCommentItemWithLikeState(
    reply: Reply,
    onLikeClick: (Reply, Boolean) -> Unit,
    onCommentClick: (Reply) -> Unit = {},
    uiState: CommentUiState?,
    modifier: Modifier = Modifier
) {
    // 计算子评论的点赞状态和数量
    val isLiked = uiState?.likedReplies?.contains(reply.replyId) ?: (reply.actionState == 1)
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
    
    ChildCommentItem(
        reply = modifiedReply,
        onLikeClick = onLikeClick,
        onCommentClick = onCommentClick,
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun ChildCommentItem(
    reply: Reply,
    onLikeClick: (Reply, Boolean) -> Unit,
    onCommentClick: (Reply) -> Unit = {},
    onUserClick: (Long) -> Unit = {},
    onBvidClick: (String) -> Unit = {},
    onAvidClick: (Long) -> Unit = {},
    onCvidClick: (Long) -> Unit = {},
    onUrlClick: (String) -> Unit = {},
    uiState: CommentUiState? = null,
    modifier: Modifier = Modifier
) {
    val isLiked = reply.actionState == 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onCommentClick(reply) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AsyncImage(
            model = reply.member.face?.takeIf { it.isNotBlank() }?.also {
                Log.d("ChildCommentAvatar", "Loading child avatar: $it for user: ${reply.member.name}")
            } ?: run {
                Log.w("ChildCommentAvatar", "Child avatar URL is null or empty for user: ${reply.member.name}")
                null
            },
            contentDescription = reply.member.name,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            onSuccess = {
                Log.d("ChildCommentAvatar", "Child avatar loaded successfully for: ${reply.member.name}")
            },
            onError = { error ->
                Log.e("ChildCommentAvatar", "Failed to load child avatar for ${reply.member.name}: ${error.result.throwable}")
            }
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reply.member.name ?: "未知用户",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLikeClick(reply, isLiked) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "点赞",
                        modifier = Modifier.size(12.dp),
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (reply.like > 0) {
                        Text(
                            text = reply.like.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var showUrlDialog by remember { mutableStateOf<String?>(null) }
            var isLoadingArticle by remember { mutableStateOf(false) }
            
            RichText(
                text = reply.content.message ?: "",
                emotes = reply.content.emote,
                atList = reply.content.atNameToMid.toList(),
                style = MaterialTheme.typography.bodyMedium,
                onUserClick = onUserClick,
                onBvidClick = onBvidClick,
                onAvidClick = onAvidClick,
                onCvidClick = { cvid ->
                    isLoadingArticle = true
                    scope.launch {
                        ArticleRedirectUtil.convertCvidToOpusId(cvid).fold(
                            onSuccess = { opusId ->
                                isLoadingArticle = false
                                onCvidClick(opusId)
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

    val childReplies = reply.replies
    if (!childReplies.isNullOrEmpty() && childReplies.size <= 3) {
        Spacer(modifier = Modifier.height(8.dp))
        childReplies.take(3).forEach { childReply ->
            ChildCommentItemWithLikeState(
                reply = childReply,
                onLikeClick = onLikeClick,
                onCommentClick = { onCommentClick(reply) }, // 点击子评论时传递主评论
                uiState = uiState,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}