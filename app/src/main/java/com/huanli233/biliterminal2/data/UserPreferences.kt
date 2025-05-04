package com.huanli233.biliterminal2.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanli233.biliterminal2.data.datastore.DataStorePreferences

const val NIGHT_MODE_FOLLOW_SYSTEM = 0
const val NIGHT_MODE_DISABLED = 1
const val NIGHT_MODE_ENABLED = 2

object PreferenceKeys {
    val ACTIVE_ACCOUNT_ID = longPreferencesKey("active_account_id").default(0)
    val FIRST_RUN = booleanPreferencesKey("first_run").default(true)

    val ROUND_MODE = booleanPreferencesKey("round_mode").default(false)
    val UI_SCALE = floatPreferencesKey("ui_scale").default(1.0f)
    val UI_PADDING_HORIZONTAL = intPreferencesKey("ui_padding_horizontal").default(0)
    val UI_PADDING_VERTICAL = intPreferencesKey("ui_padding_vertical").default(0)
    val DENSITY = intPreferencesKey("ui_density").default(0)
    val NIGHT_MODE = intPreferencesKey("night_mode").default(NIGHT_MODE_ENABLED)

    val WBI_MIXIN_KEY = stringPreferencesKey("wbi_mixin_key").default("")
    val WBI_LAST_UPDATED = longPreferencesKey("wbi_last_updated").default(0)

    val BACK_DISABLED = booleanPreferencesKey("back_disabled").default(false)
    val SNACKBAR_ENABLED = booleanPreferencesKey("snackbar_enabled").default(true)
    val ASYNC_INFLATE_ENABLED = booleanPreferencesKey("async_inflate_enabled").default(false)
    val MARQUEE_ENABLED = booleanPreferencesKey("marquee_enable").default(true)
    val STOP_LOAD_IMAGE_WHILE_SCROLLING = booleanPreferencesKey("stop_load_image_while_scrolling").default(false)
}

private fun <T> Preferences.Key<T>.default(
    defaultValue: T
) = Pair(this, defaultValue)

object UserPreferences {

    val impl: IPreferences = DataStorePreferences()

    val activeAccountId = config(PreferenceKeys.ACTIVE_ACCOUNT_ID)
    val firstRun = config(PreferenceKeys.FIRST_RUN)

    val roundMode = config(PreferenceKeys.ROUND_MODE)
    val uiScale = config(PreferenceKeys.UI_SCALE)
    val uiPaddingHorizontal = config(PreferenceKeys.UI_PADDING_HORIZONTAL)
    val uiPaddingVertical = config(PreferenceKeys.UI_PADDING_VERTICAL)
    val nightMode = config(PreferenceKeys.NIGHT_MODE)
    val density = config(PreferenceKeys.DENSITY)

    val wbiMixinKey = config(PreferenceKeys.WBI_MIXIN_KEY)
    val wbiLastUpdated = config(PreferenceKeys.WBI_LAST_UPDATED)

    val backDisabled = config(PreferenceKeys.BACK_DISABLED)
    val snackbarEnabled = config(PreferenceKeys.SNACKBAR_ENABLED)
    val asyncInflateEnabled = config(PreferenceKeys.ASYNC_INFLATE_ENABLED)
    val marqueeEnabled = config(PreferenceKeys.MARQUEE_ENABLED)
    val stopLoadImageWhileScrolling = config(PreferenceKeys.STOP_LOAD_IMAGE_WHILE_SCROLLING)

    private fun <T> config(
        /* key, defaultValue */ pair: Pair<Preferences.Key<T>, T>
    ): Config<T> {
        return Config(
            preferences = impl,
            key = pair.first,
            defaultValue = pair.second
        )
    }

    private fun <T> config(
        key: Preferences.Key<T>
    ): Config<T> {
        return Config(
            preferences = impl,
            key = key,
            defaultValue = null
        )
    }

}

class Config<T>(
    private val preferences: IPreferences,
    private val key: Preferences.Key<T>,
    private val defaultValue: T?
) {

    fun get(): T {
        return preferences.get(key) ?: defaultValue ?: throw IllegalArgumentException()
    }

    suspend fun set(value: T) {
        preferences.set(key, value)
    }
}