package com.huanli233.biliterminal2.ui.fragment.setting

import android.util.Log
import androidx.preference.PreferenceDataStore
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.proto.NightMode
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.data.setting.edit
import kotlinx.coroutines.launch

class SettingsDataStore: PreferenceDataStore() {

    override fun getString(key: String?, defValue: String?): String? {
        return LocalData.settings.run {
            when (key) {
                "ui_scale" -> uiSettings.uiScale.toString()
                "ui_padding_horizontal" -> uiSettings.uiPaddingHorizontal.toString()
                "ui_padding_vertical" -> uiSettings.uiPaddingVertical.toString()
                "density" -> uiSettings.density.toString()
                "night_mode" -> when (theme.nightMode) {
                    NightMode.NIGHT_MODE_AUTO -> 0
                    NightMode.NIGHT_MODE_DAY -> 1
                    else -> 2
                }.toString()
                else -> defValue
            }
        }
    }

    override fun putString(key: String?, value: String?) {
        Log.d("SettingsDataStore", "putString: $key $value")
        applicationScope.launch {
            LocalData.edit {
                when (key) {
                    "ui_scale" -> uiSettings = uiSettings.edit { uiScale = value?.toFloatOrNull() ?: 1f }
                    "ui_padding_horizontal" -> uiSettings = uiSettings.edit { uiPaddingHorizontal = value?.toIntOrNull() ?: 0 }
                    "ui_padding_vertical" -> uiSettings = uiSettings.edit { uiPaddingVertical = value?.toIntOrNull() ?: 0 }
                    "density" -> uiSettings = uiSettings.edit { density = value?.toIntOrNull() ?: 0 }
                    "night_mode" -> theme = theme.edit {
                        nightMode = when (value?.toIntOrNull()) {
                            0 -> NightMode.NIGHT_MODE_AUTO
                            1 -> NightMode.NIGHT_MODE_DAY
                            else -> NightMode.NIGHT_MODE_NIGHT
                        }
                    }
                }
            }
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return LocalData.settings.run {
            when (key) {
                "round_mode" -> uiSettings.roundMode
                "disable_fullscreen_dialog" -> theme.fullScreenDialogDisabled
                "animations" -> theme.animationsEnabled
                "theme_color_system" -> theme.followSystemAccent
                else -> defValue
            }
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        applicationScope.launch {
            LocalData.edit {
                when (key) {
                    "round_mode" -> uiSettings = uiSettings.edit { roundMode = value }
                    "disable_fullscreen_dialog" -> theme = theme.edit { fullScreenDialogDisabled = value }
                    "animations" -> theme = theme.edit { animationsEnabled = value }
                    "theme_color_system" -> theme = theme.edit { followSystemAccent = value }
                }
            }
        }
    }

}