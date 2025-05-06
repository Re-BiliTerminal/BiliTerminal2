package com.huanli233.biliterminal2.data.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.data.account.AccountManager
import com.huanli233.biliterminal2.ui.activity.recommend.RecommendActivity
import kotlin.collections.firstOrNull

val DEFAULT_MENU_LIST = listOf<MenuItem>(
    menuItem<RecommendActivity>("recommend", R.string.recommend, R.drawable.icon_featured_play_list),
)

inline fun <reified T> menuItem(
    id: String,
    @StringRes title: Int,
    @DrawableRes icon: Int,
    requireLoggedIn: Boolean = false
): MenuItem {
    return MenuItem(T::class.java, id, icon, title, requireLoggedIn)
}

data class MenuItem(
    val activityClass: Class<*>,
    val id: String,
    val title: Int,
    val icon: Int,
    val requireLoggedIn: Boolean
)

data class MenuConfig(
    val list: List<String> = emptyList()
) {
    val menuItems: List<MenuItem>
        get() = list.map { id ->
            DEFAULT_MENU_LIST.first { it.id == id }
        }

    val firstActivityClass: Class<*>
        get() {
            return findFirstAvailableActivity(MenuConfigManager.readMenuConfig().menuItems)
                 ?: findFirstAvailableActivity(DEFAULT_MENU_LIST) ?: throw IllegalStateException("No menu activity available.")
        }

    private fun findFirstAvailableActivity(
        menuItems: List<MenuItem>
    ): Class<*>? {
        return menuItems
            .firstOrNull { !it.requireLoggedIn || AccountManager.currentAccount.accountId != 0L }
            ?.activityClass
    }

    override fun toString(): String {
        return MenuConfigManager.toString(this)
    }
}

object MenuConfigManager {

    fun readMenuConfig(): MenuConfig {
        fromString(DataStore.appSettings.menuConfig)?.let { parsed ->
            return parsed
        } ?: return MenuConfig(
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