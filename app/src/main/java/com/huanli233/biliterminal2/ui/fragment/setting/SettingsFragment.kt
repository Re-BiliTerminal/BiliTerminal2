package com.huanli233.biliterminal2.ui.fragment.setting

import android.os.Bundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.preferences.BasePreferenceFragment

const val ARG_KEY_PREFERENCE_RESOURCE_ID = "preference_resource_id"

open class SettingsFragment: BasePreferenceFragment() {

    private val _xmlResourceID by lazy {
        arguments?.getInt(ARG_KEY_PREFERENCE_RESOURCE_ID) ?: getXmlResourceID()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(_xmlResourceID, rootKey)
    }

    open fun getXmlResourceID(): Int {
        return R.xml.preferences_main
    }

}