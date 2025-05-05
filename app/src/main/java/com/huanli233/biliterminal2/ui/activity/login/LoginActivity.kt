package com.huanli233.biliterminal2.ui.activity.login

import android.os.Bundle
import android.os.PersistableBundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityCommonViewpagerBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.fragment.login.QrCodeLoginFragment
import com.huanli233.biliterminal2.ui.fragment.setup.WelcomeFragment
import com.huanli233.biliterminal2.ui.widget.pager.setupWithDotsIndicator
import com.huanli233.biliterminal2.utils.viewpager2.PagerFragmentStateAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity: BaseActivity() {

    private lateinit var binding: ActivityCommonViewpagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = PagerFragmentStateAdapter(this@LoginActivity, listOf(QrCodeLoginFragment()))
            setupWithDotsIndicator(binding.dotsIndicator)
        }

        pageName = getString(R.string.login)
    }

}