package com.huanli233.bilizepam.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun CoinDialog(
    onDismiss: () -> Unit,
    onConfirm: (count: Int, alsoLike: Boolean) -> Unit,
    maxCoins: Int = 2
) {
    var selectedCount by remember { mutableIntStateOf(1) }
    var alsoLike by remember { mutableStateOf(false) }

    AdaptDialog(
        onDismissRequest = onDismiss,
        confirmButton = { close ->
            TextButton(
                onClick = {
                    onConfirm(selectedCount, alsoLike)
                    close()
                }
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
            Text("投币")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "选择投币数量",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    (1..maxCoins).forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = selectedCount == count,
                                    onClick = { selectedCount = count },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCount == count,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("投 $count 币")
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = alsoLike,
                        onCheckedChange = { alsoLike = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("同时点赞")
                }
            }
        }
    )
}
