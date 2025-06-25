package com.huanli233.bilizepam.ui.activity.setting

import android.os.Bundle
import androidx.fragment.app.commit
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.databinding.ActivityCommonFragmentContainerBinding
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.fragment.setting.UiSettingsFragment

class UiSettingsActivity: BaseActivity() {

    private lateinit var binding: ActivityCommonFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pageName = getString(R.string.settings_ui)

        supportFragmentManager.commit {
            replace(R.id.fragment_container, UiSettingsFragment())
        }
    }

}