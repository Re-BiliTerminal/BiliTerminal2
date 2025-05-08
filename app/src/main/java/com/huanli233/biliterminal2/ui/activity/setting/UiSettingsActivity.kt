package com.huanli233.biliterminal2.ui.activity.setting

import android.os.Bundle
import androidx.fragment.app.commit
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityCommonFragmentContainerBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.fragment.setting.ARG_KEY_PREFERENCE_RESOURCE_ID
import com.huanli233.biliterminal2.ui.fragment.setting.SettingsFragment
import com.huanli233.biliterminal2.utils.extensions.putArgument

class UiSettingsActivity: BaseActivity() {

    private lateinit var binding: ActivityCommonFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pageName = getString(R.string.settings_ui)

        supportFragmentManager.commit {
            replace(R.id.fragment_container, SettingsFragment().putArgument {
                putInt(ARG_KEY_PREFERENCE_RESOURCE_ID, R.xml.preferences_ui)
            })
        }
    }

}