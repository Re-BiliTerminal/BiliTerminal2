@file:Suppress("DEPRECATION")

package com.huanli233.biliterminal2.ui.preferences

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceRecyclerViewAccessibilityDelegate
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.fragment.setting.SettingsDataStore

abstract class BasePreferenceFragment: PreferenceFragmentCompat() {

    @SuppressLint("RestrictedApi")
    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        return (inflater.inflate(R.layout.widget_settings_recyclerview, parent, false) as RecyclerView).apply {
            layoutManager = onCreateLayoutManager()
            setAccessibilityDelegateCompat(PreferenceRecyclerViewAccessibilityDelegate(this))
        }
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore()
    }

}