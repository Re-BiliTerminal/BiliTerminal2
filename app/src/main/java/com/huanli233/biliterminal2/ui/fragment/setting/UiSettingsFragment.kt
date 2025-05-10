package com.huanli233.biliterminal2.ui.fragment.setting

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import com.google.android.material.color.DynamicColors
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.ui.preferences.BasePreferenceFragment
import com.huanli233.biliterminal2.ui.preferences.MaterialSwitchPreference
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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