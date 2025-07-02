package com.huanli233.bilizepam.ui.fragment.setting

import android.os.Bundle
import androidx.preference.Preference
import com.huanli233.bilizepam.BuildConfig
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.preferences.BasePreferenceFragment

const val ARG_KEY_PREFERENCE_RESOURCE_ID = "preference_resource_id"

open class SettingsFragment: BasePreferenceFragment() {

    private val _xmlResourceID by lazy {
        arguments?.getInt(ARG_KEY_PREFERENCE_RESOURCE_ID) ?: getXmlResourceID()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(_xmlResourceID, rootKey)
        findPreference<Preference>("debug_crash")?.apply {
            isVisible = BuildConfig.DEBUG
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                throw RuntimeException("Test Crash")
            }
        }
    }

    open fun getXmlResourceID(): Int {
        return R.xml.preferences_main
    }

}