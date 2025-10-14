package com.huanli233.bilizepam.ui.screens.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class DownloadTask(
    val id: String,
    val title: String,
    val part: String,
    val cover: String,
    val progress: Float,
    val status: DownloadStatus
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadListScreen(
    onNavigateBack: () -> Unit
) {
    // TODO: 从 ViewModel 获取下载任务列表
    val downloadTasks = remember { mutableStateListOf<DownloadTask>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("下载管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (downloadTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "暂无下载任务",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "在视频详情页点击缓存按钮开始下载",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloadTasks, key = { it.id }) { task ->
                    DownloadTaskItem(
                        task = task,
                        onPauseResume = { /* TODO */ },
                        onDelete = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskItem(
    task: DownloadTask,
    onPauseResume: () -> Unit,
    onDelete: () -> Unit
) {
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
                .padding(12.dp)
        ) {
            // 标题和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (task.part.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.part,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 暂停/继续按钮
                    if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PAUSED) {
                        IconButton(
                            onClick = onPauseResume,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (task.status == DownloadStatus.DOWNLOADING) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (task.status == DownloadStatus.DOWNLOADING) "暂停" else "继续"
                            )
                        }
                    }
                    
                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            Column {
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (task.status) {
                            DownloadStatus.PENDING -> "等待中"
                            DownloadStatus.DOWNLOADING -> "下载中"
                            DownloadStatus.PAUSED -> "已暂停"
                            DownloadStatus.COMPLETED -> "已完成"
                            DownloadStatus.FAILED -> "下载失败"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
