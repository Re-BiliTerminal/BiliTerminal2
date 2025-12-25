package com.huanli233.bilizepam.data.menu

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.Gson
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.navigation.NavRoutes
import com.huanli233.bilizepam.ui.navigation.Screen

val DEFAULT_MENU_LIST = listOf(
    menuItem("login", NavRoutes.LOGIN, R.string.login, Icons.AutoMirrored.Outlined.Login, requireNotLoggedIn = true, notMenuActivity = true),
    menuItem("recommend", Screen.Recommend.route, R.string.recommend, Icons.AutoMirrored.Outlined.PlaylistPlay),
    menuItem("dynamic", Screen.Dynamic.route, R.string.dynamic, Icons.Outlined.Newspaper, requireLoggedIn = true),
    menuItem("download_manager", Screen.DownloadList.route, R.string.download_manager, Icons.Outlined.Download),
    menuItem("my_space", Screen.MySpace.route, R.string.my_space, Icons.Outlined.Person, requireLoggedIn = true),
    menuItem("search", Screen.Search.route, R.string.search, Icons.Outlined.Search),
    menuItem("settings", Screen.Settings.route, R.string.settings, Icons.Outlined.Settings, required = true)
)

fun menuItem(
    id: String,
    destination: String,
    @StringRes title: Int,
    icon: ImageVector,
    requireNotLoggedIn: Boolean = false,
    requireLoggedIn: Boolean = false,
    required: Boolean = false,
    notMenuActivity: Boolean = false
): MenuItem {
    return MenuItem(
        id,
        destination,
        title,
        icon,
        requireNotLoggedIn,
        requireLoggedIn,
        required,
        notMenuActivity
    )
}

data class MenuItem(
    val id: String,
    val destination: String,
    val title: Int,
    val icon: ImageVector,
    val requireNotLoggedIn: Boolean = false,
    val requireLoggedIn: Boolean = false,
    val required: Boolean = false,
    val notMenuActivity: Boolean = false,
)

data class MenuConfig(
    val list: List<String> = emptyList()
) {
    val menuItems: List<MenuItem>
        get() = list.mapNotNull { id ->
            DEFAULT_MENU_LIST.firstOrNull { it.id == id }
        }.filter {
            val loggedIn = AccountManager.loggedIn()
            (!it.requireLoggedIn || loggedIn) && (!it.requireNotLoggedIn || !loggedIn)
        }

    val firstDestination: String
        get() {
            return findFirstDestination(MenuConfigManager.readMenuConfig().menuItems)
                ?: findFirstDestination(DEFAULT_MENU_LIST) ?: throw IllegalStateException("No menu page available.")
        }

    private fun findFirstDestination(
        menuItems: List<MenuItem>
    ): String? {
        return menuItems
            .firstOrNull { !it.requireNotLoggedIn }
            ?.destination
    }

    override fun toString(): String {
        return MenuConfigManager.toString(this)
    }
}

object MenuConfigManager {

    fun readMenuConfig(): MenuConfig {
        return fromString(LocalData.settings.menuConfig)?.takeIf {
            it.list.isNotEmpty() && it.menuItems.containsAll(DEFAULT_MENU_LIST.filter { item -> item.required })
        } ?: MenuConfig(
            list = DEFAULT_MENU_LIST.map { it.id }
        )
    }

    fun fromString(content: String): MenuConfig? {
        return runCatching {
            Gson().fromJson(content, MenuConfig::class.java)
        }.getOrNull()
    }

    fun toString(config: MenuConfig): String {
        return Gson().toJson(config)
    }
}