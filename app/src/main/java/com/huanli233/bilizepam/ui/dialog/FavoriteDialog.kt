package com.huanli233.bilizepam.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanli233.biliwebapi.bean.video.FavoriteFolder

@Composable
fun FavoriteDialog(
    folders: List<FavoriteFolder>,
    onDismiss: () -> Unit,
    onConfirm: (selectedFids: List<Long>, deselectedFids: List<Long>) -> Unit,
    isLoading: Boolean = false
) {
    val folderStates = remember(folders) {
        mutableStateMapOf<Long, Boolean>().apply {
            folders.forEach { folder ->
                this[folder.fid] = folder.favState == 1
            }
        }
    }

    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { close ->
            TextButton(
                onClick = {
                    val selectedFids = folderStates.filter { it.value && folders.find { f -> f.fid == it.key }?.favState != 1 }.keys.toList()
                    val deselectedFids = folderStates.filter { !it.value && folders.find { f -> f.fid == it.key }?.favState == 1 }.keys.toList()
                    onConfirm(selectedFids, deselectedFids)
                    close()
                },
                enabled = !isLoading
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = {
            Text("收藏到")
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(folders) { folder ->
                            FavoriteFolderItem(
                                folder = folder,
                                isSelected = folderStates[folder.fid] ?: false,
                                onToggle = {
                                    folderStates[folder.fid] = !(folderStates[folder.fid] ?: false)
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun FavoriteFolderItem(
    folder: FavoriteFolder,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = folder.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f)
            )
            
            Checkbox(
                checked = isSelected,
                onCheckedChange = null
            )
        }
    }
}
