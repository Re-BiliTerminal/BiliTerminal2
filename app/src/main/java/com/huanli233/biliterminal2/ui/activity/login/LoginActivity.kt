package com.huanli233.biliterminal2.ui.activity.login

import android.os.Bundle
import android.os.PersistableBundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityCommonViewpagerBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.fragment.login.ImportLoginFragment
import com.huanli233.biliterminal2.ui.fragment.login.QrCodeLoginFragment
import com.huanli233.biliterminal2.ui.fragment.setup.WelcomeFragment
import com.huanli233.biliterminal2.ui.widget.pager.setupWithDotsIndicator
import com.huanli233.biliterminal2.ui.utils.viewpager2.PagerFragmentStateAdapter
import com.huanli233.biliterminal2.ui.utils.viewpager2.pagerFragmentAdapter
import com.huanli233.biliterminal2.utils.extensions.putArgument
import dagger.hilt.android.AndroidEntryPoint

const val EXTRA_NAME_FROM_SETUP = "from_setup"

@AndroidEntryPoint
class LoginActivity: BaseActivity() {

    private lateinit var binding: ActivityCommonViewpagerBinding

    private val fromSetup by lazy { intent.getBooleanExtra(EXTRA_NAME_FROM_SETUP, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = pagerFragmentAdapter(
                listOf(
                    QrCodeLoginFragment().putArgument {
                        putBoolean(EXTRA_NAME_FROM_SETUP, fromSetup)
                    },
                    ImportLoginFragment().putArgument {
                        putBoolean(EXTRA_NAME_FROM_SETUP, fromSetup)
                    }
                )
            )
            setupWithDotsIndicator(binding.dotsIndicator)
        }

        pageName = getString(R.string.login)
    }

    fun navigateToImport() {
        binding.viewPager.setCurrentItem(1, true)
    }

}