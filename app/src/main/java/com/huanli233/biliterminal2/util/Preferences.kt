package com.huanli233.biliterminal2.util

import android.content.SharedPreferences

/**
 * 被 luern0313 创建于 2020/5/4.
 * #以下代码部分来源于腕上哔哩的开源项目，有修改。感谢开源者做出的贡献！
 */
object Preferences {
    const val LINK_ENABLE: String = "link_enable"
    const val RCMD_API_NEW_PARAM: String = "rcmd_api_new_param"
    const val MENU_SORT: String = "menu_sort"
    const val ASYNC_INFLATE_ENABLE: String = "async_inflate_enable"
    const val LOAD_TRANSITION: String = "load_transition"
    const val SNACKBAR_ENABLE: String = "snackbar_enable"
    const val STRICT_URL_MATCH: String = "strict_url_match"
    const val NO_VIP_COLOR: String = "no_vip_color"
    const val NO_MEDAL: String = "no_medal"
    const val REPLY_MARQUEE_NAME: String = "reply_marquee_name"
    const val COOKIES: String = "cookies"
    const val MID: String = "mid"
    const val CSRF: String = "csrf"
    const val ACCESS_KEY: String = "access_key"
    const val REFRESH_TOKEN: String = "refresh_token"
    const val SETUP: String = "setup"
    const val PLAYER: String = "player"
    const val PADDING_HORIZONTAL: String = "padding_horizontal"
    const val PADDING_VERTICAL: String = "padding_vertical"
    const val COOKIE_REFRESH: String = "cookie_refresh"
    const val SEARCH_HISTORY: String = "search_history"
    const val COVER_PLAY_ENABLE: String = "cover_play_enable"
    const val TUTORIAL_VERSION: String = "tutorial_version"
    const val WBI_LAST_UPDATED = "wbi_last_updated"
    const val WBI_MIXIN_KEY = "wbi_mixin_key"

    var sharedPreferences: SharedPreferences? = null

    @JvmStatic
    fun getString(key: String, def: String): String {
        return sharedPreferences!!.getString(key, def) ?: def
    }

    @JvmStatic
    fun putString(key: String, value: String) {
        sharedPreferences!!.edit().putString(key, value).apply()
    }

    @JvmStatic
    fun getInt(key: String, def: Int): Int {
        return sharedPreferences!!.getInt(key, def)
    }

    @JvmStatic
    fun putInt(key: String, value: Int) {
        sharedPreferences!!.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun getLong(key: String, def: Long): Long {
        return sharedPreferences!!.getLong(key, def)
    }

    @JvmStatic
    fun putLong(key: String, value: Long) {
        sharedPreferences!!.edit().putLong(key, value).apply()
    }

    @JvmStatic
    fun getBoolean(key: String, def: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, def)
    }

    @JvmStatic
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences!!.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun putFloat(key: String, value: Float) {
        sharedPreferences!!.edit().putFloat(key, value).apply()
    }

    @JvmStatic
    fun getFloat(key: String, def: Float): Float {
        return sharedPreferences!!.getFloat(key, def)
    }

    @JvmStatic
    fun removeValue(key: String) {
        sharedPreferences!!.edit().remove(key).apply()
    }
}
