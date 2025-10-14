package com.huanli233.bilizepam.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class VideoPage(
    val cid: Long,
    val page: Int,
    val part: String,
    val duration: Long
)

@Composable
fun DownloadDialog(
    pages: List<VideoPage>,
    onDismiss: () -> Unit,
    onConfirm: (selectedPages: List<VideoPage>) -> Unit
) {
    val selectedPages = remember { mutableStateMapOf<Int, Boolean>() }
    
    // 默认选中第一个
    LaunchedEffect(pages) {
        if (pages.isNotEmpty() && selectedPages.isEmpty()) {
            selectedPages[0] = true
        }
    }

    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { close ->
            TextButton(
                onClick = {
                    val selected = pages.filterIndexed { index, _ -> 
                        selectedPages[index] == true 
                    }
                    onConfirm(selected)
                    close()
                },
                enabled = selectedPages.values.any { it }
            ) {
                Text("开始下载")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = {
            Text("选择要下载的分P")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            pages.indices.forEach { index ->
                                selectedPages[index] = true
                            }
                        }
                    ) {
                        Text("全选")
                    }
                    
                    TextButton(
                        onClick = {
                            selectedPages.clear()
                        }
                    ) {
                        Text("取消全选")
                    }
                }
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(pages) { index, page ->
                        VideoPageItem(
                            page = page,
                            isSelected = selectedPages[index] == true,
                            onToggle = {
                                selectedPages[index] = !(selectedPages[index] ?: false)
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun VideoPageItem(
    page: VideoPage,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "P${page.page} ${page.part}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Text(
                    text = formatDuration(page.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = null
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
