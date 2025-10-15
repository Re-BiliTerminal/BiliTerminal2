package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.materialcore.toVerticalPadding
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.components.WearTopBar
import com.huanli233.bilizepam.ui.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScalingLazyListState(initialCenterItemIndex = 0)

    ScreenScaffold(scrollState = scrollState) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = it.toVerticalPadding()
        ) {
            item {
                WearTopBar(
                    title = stringResource(id = R.string.settings),
                    showBackIcon = true
                )
            }
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
                    icon = Icons.Outlined.Info,
                    title = stringResource(id = R.string.about),
                    onClick = { navController.navigate(Screen.About.route) }
                )
            }
        }
    }
}