package com.huanli233.bilizepam.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.foundation.padding
import androidx.compose.material3.MaterialTheme
import androidx.wear.compose.material3.PaddingDefaults
import androidx.wear.compose.material3.TimeText
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData

@Composable
fun WearTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    val isRound = isRoundDevice()

    if (isRound) {
        RoundTopBar(
            title = title,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    } else {
        SquareTopBar(
            title = title,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            modifier = modifier
        )
    }
}

@Composable
private fun RoundTopBar(
    title: String,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    onBackClick: (() -> Unit)?,
    onMenuClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val statusBarHeight = systemBarsPadding.calculateTopPadding()
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 状态栏背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        )
        
        // 内容区域
        Log.d("RoundTopBar", "paddings: $statusBarHeight ${PaddingDefaults.verticalOptContentPadding()} ${PaddingDefaults.horizontalOptContentPadding()}")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    )
                )
                .clickable(enabled = onBackClick != null || onMenuClick != null) {
                    onBackClick?.invoke() ?: onMenuClick?.invoke()
                }
                .padding(
                    start = 12.dp + PaddingDefaults.horizontalContentPadding(),
                    end = 12.dp + PaddingDefaults.horizontalContentPadding(),
                    top = statusBarHeight + 4.dp + PaddingDefaults.verticalContentPadding(),
                    bottom = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (showBackIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_keyboard_arrow_left),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (showMenuIcon) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SquareTopBar(
    title: String,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    onBackClick: (() -> Unit)?,
    onMenuClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val statusBarHeight = systemBarsPadding.calculateTopPadding()
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 状态栏背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeight)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        )
        
        // TopBar 背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .offset(y = statusBarHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // 内容区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onBackClick != null || onMenuClick != null) {
                    onBackClick?.invoke() ?: onMenuClick?.invoke()
                }
                .padding(
                    start = 12.dp + PaddingDefaults.horizontalOptContentPadding(),
                    end = 12.dp + PaddingDefaults.horizontalOptContentPadding(),
                    top = statusBarHeight + 8.dp,
                    bottom = 8.dp
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (showBackIcon) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_keyboard_arrow_left),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showMenuIcon) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    }
}
