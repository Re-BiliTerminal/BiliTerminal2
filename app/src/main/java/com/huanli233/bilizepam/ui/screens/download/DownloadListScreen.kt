package com.huanli233.bilizepam.ui.screens.download

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.huanli233.bilizepam.data.download.DownloadEntity
import com.huanli233.bilizepam.data.download.DownloadStatus
import com.huanli233.bilizepam.R
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.plus
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.dialog.AdaptDialog
import com.huanli233.bilizepam.ui.screens.recommend.LoadingState
import com.huanli233.bilizepam.ui.screens.recommend.LoadingView
import com.huanli233.bilizepam.ui.viewmodel.DownloadListViewModel

enum class ContentState {
    EMPTY,
    CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadListScreen(
    onNavigateBack: () -> Unit,
    viewModel: DownloadListViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsState()
    var deleteTarget by remember { mutableStateOf<DownloadEntity?>(null) }
    var deleteFile by remember { mutableStateOf(false) }

    val scrollState = rememberScalingLazyListState()
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    val contentState = remember(downloads) {
        if (downloads.isEmpty()) ContentState.EMPTY else ContentState.CONTENT
    }

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.download_manager),
            showBackIcon = true,
            onBackClick = onNavigateBack,
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Crossfade(
            targetState = contentState,
            animationSpec = tween(durationMillis = 300),
            label = "DownloadListContentStateTransition",
            modifier = Modifier.fillMaxSize()
        ) { state ->
            when (state) {
                ContentState.EMPTY -> {
                    LoadingView(
                        state = LoadingState.EMPTY,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                ContentState.CONTENT -> {
                    ScalingLazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        state = scrollState,
                        contentPadding = paddingValues.plus(
                            PaddingValues(horizontal = 16.dp)
                        )
                    ) {
                        items(
                            count = downloads.size,
                            key = { index -> downloads[index].id }
                        ) { index ->
                            val task = downloads[index]
                            DownloadTaskItem(
                                task = task,
                                onCancel = { viewModel.cancel(task.id) },
                                onRetry = { viewModel.retry(task.id) },
                                onDelete = { deleteTarget = task }
                            )
                        }
                    }
                }
            }
        }

        deleteTarget?.let { target ->
            AdaptDialog(
                onDismissRequest = {
                    deleteTarget = null
                    deleteFile = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.delete(target.id, deleteFile)
                            deleteTarget = null
                            deleteFile = false
                        }
                    ) {
                        Text("删除")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            deleteTarget = null
                            deleteFile = false
                        }
                    ) {
                        Text("取消")
                    }
                },
                title = { Text("删除下载") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(target.fileName)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = deleteFile, onCheckedChange = { deleteFile = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("同时删除文件")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DownloadTaskItem(
    task: DownloadEntity,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    val url = task.coverUrl
                    if (!url.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(url)
                                .crossfade(200)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = task.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = when (task.status) {
                                DownloadStatus.ENQUEUED -> "等待中"
                                DownloadStatus.RUNNING -> "下载中"
                                DownloadStatus.CANCELED -> "已取消"
                                DownloadStatus.SUCCEEDED -> "已完成"
                                DownloadStatus.FAILED -> "下载失败"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${task.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (task.status == DownloadStatus.RUNNING || task.status == DownloadStatus.ENQUEUED) {
                            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "取消")
                            }
                        }

                        if (task.status == DownloadStatus.FAILED) {
                            IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "重试")
                            }
                        }

                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (task.progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            )
        }
    }
}
