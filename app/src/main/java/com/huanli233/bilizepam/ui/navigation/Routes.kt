package com.huanli233.bilizepam.ui.navigation

import androidx.annotation.StringRes
import com.huanli233.bilizepam.R

object NavGraph {
    const val SETUP = "setup_graph"
    const val LOGIN = "login"
    const val MAIN = "main_graph"
}

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    data object Recommend : Screen("recommend", R.string.recommend)
    data object Settings : Screen("settings_root", R.string.settings)

    data object UiSettings : Screen("settings_ui", R.string.settings_ui)
    data object About : Screen("settings_about", R.string.about)
    data object ThemeColor : Screen("settings_theme_color", R.string.theme_color)
    data object ViewPreview : Screen("view_preview", R.string.view_preview)
    
    data object VideoDetail : Screen("video_detail/{avid}/{bvid}", R.string.app_name) {
        fun createRoute(avid: Long, bvid: String) = "video_detail/$avid/$bvid"
    }
}

val allScreens = listOf(
    Screen.Recommend,
    Screen.Settings,
    Screen.UiSettings,
    Screen.About,
    Screen.ThemeColor,
    Screen.ViewPreview
)