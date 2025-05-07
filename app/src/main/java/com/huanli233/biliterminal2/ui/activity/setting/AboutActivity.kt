package com.huanli233.biliterminal2.ui.activity.setting

import android.os.Bundle
import android.os.PersistableBundle
import com.huanli233.biliterminal2.databinding.ActivityAboutBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity

class AboutActivity: BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    fun initUi() {
        // TODO
    }

}