package com.huanli233.bilizepam.ui.fragment.setting

import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.color.DynamicColors
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.preferences.MaterialSwitchPreference

open class UiSettingsFragment: SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<MaterialSwitchPreference>("theme_color_system")?.apply {
            isVisible = DynamicColors.isDynamicColorAvailable()
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                activity?.recreate()
                true
            }
        }
    }

    override fun getXmlResourceID(): Int {
        return R.xml.preferences_ui
    }

}