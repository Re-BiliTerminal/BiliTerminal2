package com.huanli233.bilizepam.ui.screens.setting

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    LazyColumn {
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