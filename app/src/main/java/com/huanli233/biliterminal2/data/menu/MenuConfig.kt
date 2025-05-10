package com.huanli233.biliterminal2.data.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.data.account.AccountManager
import com.huanli233.biliterminal2.ui.activity.login.LoginActivity
import com.huanli233.biliterminal2.ui.activity.recommend.RecommendActivity
import com.huanli233.biliterminal2.ui.activity.setting.SettingsActivity
import kotlin.collections.firstOrNull

val DEFAULT_MENU_LIST = listOf<MenuItem>(
    menuItem<LoginActivity>("login", R.string.login, R.drawable.icon_login, requireNotLoggedIn = true, notMenuActivity = true),
    menuItem<RecommendActivity>("recommend", R.string.recommend, R.drawable.icon_featured_play_list),
    menuItem<SettingsActivity>("settings", R.string.settings, R.drawable.icon_settings, required = true)
)

inline fun <reified T> menuItem(
    id: String,
    @StringRes title: Int,
    @DrawableRes icon: Int,
    requireNotLoggedIn: Boolean = false,
    requireLoggedIn: Boolean = false,
    required: Boolean = false,
    notMenuActivity: Boolean = false
): MenuItem {
    return MenuItem(
        T::class.java,
        id,
        title,
        icon,
        requireNotLoggedIn,
        requireLoggedIn,
        required,
        notMenuActivity
    )
}

data class MenuItem(
    val activityClass: Class<*>,
    val id: String,
    val title: Int,
    val icon: Int,
    val requireNotLoggedIn: Boolean = false,
    val requireLoggedIn: Boolean = false,
    val required: Boolean = false,
    val notMenuActivity: Boolean = false,
)

data class MenuConfig(
    val list: List<String> = emptyList()
) {
    val menuItems: List<MenuItem>
        get() = list.map { id ->
            DEFAULT_MENU_LIST.first { it.id == id }
        }.filter {
            val loggedIn = AccountManager.loggedIn()
            (!it.requireLoggedIn || loggedIn) && (!it.requireNotLoggedIn || !loggedIn)
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
            .firstOrNull { !it.requireNotLoggedIn }
            ?.activityClass
    }

    override fun toString(): String {
        return MenuConfigManager.toString(this)
    }
}

object MenuConfigManager {

    fun readMenuConfig(): MenuConfig {
        return fromString(LocalData.settings.menuConfig)?.takeIf {
            it.list.isNotEmpty() && it.menuItems.containsAll(DEFAULT_MENU_LIST.filter { it.required })
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