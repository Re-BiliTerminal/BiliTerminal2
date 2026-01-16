package com.huanli233.bilizepam.ui.navigation

import androidx.annotation.StringRes
import com.huanli233.bilizepam.R
import java.net.URLEncoder

object NavGraph {
    const val SETUP = "setup_graph"
    const val LOGIN = "login"
    const val MAIN = "main_graph"
}

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    data object Recommend : Screen("recommend", R.string.recommend)
    data object Dynamic : Screen("dynamic", R.string.dynamic)
    data object Settings : Screen("settings_root", R.string.settings)

    data object DownloadList : Screen("download_list", R.string.download_manager)

    data object UiSettings : Screen("settings_ui", R.string.settings_ui)
    data object PlayerSettings : Screen("settings_player", R.string.settings_player)
    data object About : Screen("settings_about", R.string.about)
    data object ThemeColor : Screen("settings_theme_color", R.string.theme_color)
    data object ViewPreview : Screen("view_preview", R.string.view_preview)
    data object DeveloperOptions : Screen("settings_developer", R.string.developer_options)
    
    data object VideoDetail : Screen("video_detail/{avid}/{bvid}", R.string.app_name) {
        fun createRoute(avid: Long, bvid: String) = "video_detail/$avid/$bvid"
    }
    
    data object DynamicDetail : Screen("dynamic_detail/{dynamicId}", R.string.dynamic_detail) {
        fun createRoute(dynamicId: String) = "dynamic_detail/$dynamicId"
    }
    
    data object OpusDetail : Screen("opus_detail/{opusId}", R.string.opus_detail) {
        fun createRoute(opusId: String) = "opus_detail/$opusId"
    }
    
    data object Search : Screen("search", R.string.search)
    data object SearchResult : Screen("search_result/{keyword}", R.string.search_result) {
        fun createRoute(keyword: String): String {
            val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
            return "search_result/$encodedKeyword"
        }
    }
    
    data object MySpace : Screen("my_space", R.string.my_space)
}

val allScreens = listOf(
    Screen.Recommend,
    Screen.Dynamic,
    Screen.Settings,
    Screen.UiSettings,
    Screen.PlayerSettings,
    Screen.About,
    Screen.ThemeColor,
    Screen.ViewPreview,
    Screen.Search,
    Screen.MySpace
)