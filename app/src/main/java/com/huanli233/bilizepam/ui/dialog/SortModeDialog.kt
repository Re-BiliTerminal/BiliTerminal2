package com.huanli233.bilizepam.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.screens.comment.CommentSortMode

@Composable
fun SortModeDialog(
    currentMode: CommentSortMode,
    onModeSelected: (CommentSortMode) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScalingLazyListState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenScaffold(
            scrollState = scrollState,
            topBar = scrollAwareTopBar(
                title = "排序方式",
                showBackIcon = true,
                onBackClick = onDismiss
            )
        ) { paddingValues ->
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
                contentPadding = paddingValues,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            items(CommentSortMode.values().size) { index ->
                val mode = CommentSortMode.values()[index]
                SortModeItem(
                    mode = mode,
                    isSelected = mode == currentMode,
                    onClick = {
                        onModeSelected(mode)
                        onDismiss()
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun SortModeItem(
    mode: CommentSortMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
