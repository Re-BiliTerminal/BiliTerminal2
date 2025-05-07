package com.huanli233.biliterminal2.ui.fragment.setting

import android.os.Bundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.preferences.BasePreferenceFragment

class SettingsFragment: BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        preferenceManager.preferenceDataStore = SettingsDataStore()
        setPreferencesFromResource(R.xml.preferences_main, rootKey)
    }

}