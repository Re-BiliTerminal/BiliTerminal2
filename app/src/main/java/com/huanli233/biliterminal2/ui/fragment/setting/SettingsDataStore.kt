package com.huanli233.biliterminal2.ui.fragment.setting

import android.util.Log
import androidx.preference.PreferenceDataStore
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.proto.NightMode
import com.huanli233.biliterminal2.data.setting.DataStore
import kotlinx.coroutines.launch

class SettingsDataStore: PreferenceDataStore() {

    override fun getString(key: String?, defValue: String?): String? {
        return DataStore.appSettings.run {
            when (key) {
                "ui_scale" -> uiScale.toString()
                "ui_padding_horizontal" -> uiPaddingHorizontal.toString()
                "ui_padding_vertical" -> uiPaddingVertical.toString()
                "density" -> density.toString()
                "night_mode" -> when (nightMode) {
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
            DataStore.editData {
                when (key) {
                    "ui_scale" -> uiScale = value?.toFloatOrNull() ?: 1f
                    "ui_padding_horizontal" -> uiPaddingHorizontal = value?.toIntOrNull() ?: 0
                    "ui_padding_vertical" -> uiPaddingVertical = value?.toIntOrNull() ?: 0
                    "density" -> density = value?.toIntOrNull() ?: 0
                    "night_mode" -> nightMode = when (value?.toIntOrNull()) {
                        0 -> NightMode.NIGHT_MODE_AUTO
                        1 -> NightMode.NIGHT_MODE_DAY
                        else -> NightMode.NIGHT_MODE_NIGHT
                    }
                }
            }
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return DataStore.appSettings.run {
            when (key) {
                "round_mode" -> roundMode
                "disable_fullscreen_dialog" -> fullScreenDialogDisabled
                "animations" -> animationsEnabled
                else -> defValue
            }
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        applicationScope.launch {
            DataStore.editData {
                when (key) {
                    "round_mode" -> roundMode = value
                    "disable_fullscreen_dialog" -> fullScreenDialogDisabled = value
                    "animations" -> animationsEnabled = value
                }
            }
        }
    }

}