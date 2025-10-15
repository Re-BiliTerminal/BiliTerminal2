package com.huanli233.bilizepam.ui.components

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
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.isRoundDevice
import androidx.wear.compose.foundation.padding
import androidx.wear.compose.material3.TimeText
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData

@Composable
fun WearTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = true,
    showMenuIcon: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val isRound = isRoundDevice() && LocalData.settings.uiSettings.roundMode

    if (isRound) {
        RoundTopBar(
            title = title,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            modifier = modifier
        )
    } else {
        SquareTopBar(
            title = title,
            showBackIcon = showBackIcon,
            showMenuIcon = showMenuIcon,
            modifier = modifier
        )
    }
}

@Composable
private fun RoundTopBar(
    title: String,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    modifier: Modifier = Modifier
) {
    // 圆屏模式：只显示标题栏，TimeText由ScreenScaffold自动处理
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showBackIcon) {
            Icon(
                painter = painterResource(id = R.drawable.icon_keyboard_arrow_left),
                contentDescription = "Back",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (showMenuIcon) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Menu",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SquareTopBar(
    title: String,
    showBackIcon: Boolean,
    showMenuIcon: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
