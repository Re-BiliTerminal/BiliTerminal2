package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = if (summary != null) { { Text(summary) } } else null,
        leadingContent = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else null,
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}

@Composable
fun SwitchSettingsItem(
    icon: Painter? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onCheckedChange(!checked) },
        headlineContent = { Text(title) },
        supportingContent = if (summary != null) { { Text(summary) } } else null,
        leadingContent = if (icon != null) {
            {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else null,
        trailingContent = {
            Switch(checked = checked, onCheckedChange = null)
        },
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}