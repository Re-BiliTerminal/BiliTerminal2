package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.toVerticalPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.scrollAwareTopBar
import com.huanli233.bilizepam.ui.components.rememberEnterAlwaysScrollBehavior
import com.huanli233.bilizepam.ui.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    
    // Create ScrollBehavior for TopBar
    val scrollBehavior = rememberEnterAlwaysScrollBehavior()

    ScreenScaffold(
        scrollState = scrollState,
        topBar = scrollAwareTopBar(
            title = stringResource(id = R.string.settings),
            showBackIcon = true,
            onBackClick = { navController.popBackStack() },
            scrollBehavior = scrollBehavior
        ),
        topBarScrollBehavior = scrollBehavior
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState,
                contentPadding = paddingValues
            ) {
            item {
                SettingsCategory(title = stringResource(id = R.string.preference))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(id = R.string.settings_ui),
                    onClick = { navController.navigate(Screen.UiSettings.route) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.PlayArrow,
                    title = stringResource(id = R.string.settings_player),
                    onClick = { navController.navigate(Screen.PlayerSettings.route) }
                )
            }
                item {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = stringResource(id = R.string.about),
                        onClick = { navController.navigate(Screen.About.route) }
                    )
                }
            }
        }
    }
}