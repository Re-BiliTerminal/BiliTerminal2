package com.huanli233.biliterminal2.ui.activity.setup

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.databinding.ActivitySetupBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.login.EXTRA_NAME_FROM_SETUP
import com.huanli233.biliterminal2.ui.activity.login.LoginActivity
import kotlinx.coroutines.launch
import splitties.activities.start

class SetupActivity: BaseActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var navController: NavController
    private var currentFragmentId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        initUi()
    }

    fun initUi() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            currentFragmentId = destination.id
            when (currentFragmentId) {
                R.id.welcomeFragment -> {
                    binding.nextButton.setImageResource(R.drawable.icon_chevron_right)
                    pageName = getString(R.string.welcome)
                }
                R.id.uiSetupFragment -> {
                    binding.nextButton.setImageResource(R.drawable.icon_check)
                    pageName = getString(R.string.initialize_setting)
                }
            }
        }
        binding.topBar.setOnClickListener {
            if (currentFragmentId == R.id.welcomeFragment) {
                finish()
            } else {
                navController.popBackStack()
            }
        }
        binding.nextButton.setOnClickListener {
            when (currentFragmentId) {
                R.id.welcomeFragment -> {
                    navController.navigate(R.id.action_toUiFragment)
                }
                R.id.uiSetupFragment -> {
                    lifecycleScope.launch {
                        DataStore.editData {
                            firstRun = false
                        }
                        finish()
                        start<LoginActivity> {
                            putExtra(EXTRA_NAME_FROM_SETUP, true)
                        }
                    }
                }
            }
        }
    }

}