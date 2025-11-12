package com.huanli233.bilizepam.ui.components.menu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
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
    ), onSelect = {}, onDismiss = {})
}

@Composable
fun MenuPanel(
    modifier: Modifier = Modifier,
    menuItems: List<MenuItem>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val loggedIn = remember { AccountManager.loggedIn() }
    val scrollState = rememberScalingLazyListState()
    
    val filteredItems = remember(menuItems, loggedIn) {
        menuItems.filter { item ->
            (!item.requireLoggedIn || loggedIn) &&
            (!item.requireNotLoggedIn || !loggedIn)
        }
    }
    
    val horizontalScrollBlocker = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (abs(available.x) > abs(available.y)) {
                    Offset(available.x, 0f)
                } else {
                    Offset.Zero
                }
            }
            
            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (abs(available.x) > abs(available.y)) {
                    Velocity(available.x, 0f)
                } else {
                    Velocity.Zero
                }
            }
        }
    }

    BackHandler(onBack = onDismiss)

    ScreenScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        scrollState = scrollState
    ) {
        ScalingLazyColumn(
            modifier = modifier
                .fillMaxSize()
                .systemGestureExclusion()
                .nestedScroll(horizontalScrollBlocker)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: continue
                            val dragX = change.position.x - change.previousPosition.x
                            val dragY = change.position.y - change.previousPosition.y
                            
                            if (abs(dragX) > abs(dragY) && abs(dragX) > 0) {
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                },
            state = scrollState,
            contentPadding = it
        ) {
            item {
                ListHeader {
                    Text(
                        text = stringResource(R.string.menu),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(filteredItems.size) { index ->
                val item = filteredItems[index]
                androidx.wear.compose.material.Chip(
                    label = {
                        Text(
                            text = stringResource(id = item.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = { onSelect(item.destination) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}