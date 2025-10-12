package com.huanli233.bilizepam.ui.components.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.menu.MenuItem
import com.huanli233.bilizepam.data.menu.menuItem

@Preview
@Composable
fun MenuPanelPreview() {
    MenuPanel(menuItems = listOf(
        menuItem(
            id = "1",
            destination = "recommend",
            title = R.string.recommend,
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay
        ),
        menuItem(
            id = "2",
            destination = "setting",
            title = R.string.settings,
            icon = Icons.Default.Settings
        ),
    ), onSelect = {})
}

@Composable
fun MenuPanel(modifier: Modifier = Modifier, menuItems: List<MenuItem>, onSelect: (String) -> Unit) {
    val loggedIn = remember { AccountManager.loggedIn() }
    Column(modifier.fillMaxWidth().padding(16.dp)) {
        menuItems.forEachIndexed { index, item ->
            if ((!item.requireLoggedIn && !loggedIn) ||
                (!item.requireNotLoggedIn && loggedIn)) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 300, delayMillis = index * 50)
                    ) + slideInHorizontally(
                        initialOffsetX = { -it / 2 },
                        animationSpec = tween(durationMillis = 300, delayMillis = index * 50)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp, horizontal = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelect(item.destination) }
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(id = item.title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}