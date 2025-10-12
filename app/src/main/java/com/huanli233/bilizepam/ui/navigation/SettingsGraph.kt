package com.huanli233.bilizepam.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.huanli233.bilizepam.ui.screens.setting.AboutScreen
import com.huanli233.bilizepam.ui.screens.setting.SettingsScreen
import com.huanli233.bilizepam.ui.screens.setting.ThemeColorScreen
import com.huanli233.bilizepam.ui.screens.setting.UiSettingsScreen

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(
        route = NavRoutes.SETTINGS,
        startDestination = Screen.Settings.route
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }
        composable(Screen.UiSettings.route) {
            UiSettingsScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen()
        }
        composable(Screen.ThemeColor.route) {
            ThemeColorScreen()
        }
        composable(Screen.ViewPreview.route) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}