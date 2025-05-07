package com.huanli233.biliterminal2.ui.activity.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.activity.base.BaseMenuActivity
import com.huanli233.biliterminal2.ui.fragment.setting.SettingsFragment

class SettingsActivity: BaseMenuActivity() {

    override fun getMenuName(): String = getString(R.string.settings)

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return SettingsFragment()
    }

}