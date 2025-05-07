package com.huanli233.biliterminal2.ui.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.menu.MenuConfigManager
import com.huanli233.biliterminal2.databinding.ActivityMainBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.setup.SetupActivity
import com.huanli233.biliterminal2.ui.utils.crossFadeSetText
import com.huanli233.biliterminal2.utils.extensions.invisible
import com.huanli233.biliterminal2.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import splitties.activities.start

@AndroidEntryPoint
class MainActivity: BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BiliTerminal2_Material3)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.initializationState.observe(this) { state ->
            when (state) {
                InitializationState.Loading -> showLoadingView()
                InitializationState.Success -> runAfterInitialized()
                is InitializationState.Error -> showErrorView(state.message)
                InitializationState.NavigateToSetup -> navigateToSetup()
            }
        }

        binding.errorImageView.setOnClickListener { viewModel.retryInitialization() }
        binding.errorTextView.setOnClickListener { viewModel.retryInitialization() }
    }

    private fun showLoadingView() {
        binding.textView.visible()
        binding.progressIndicator.visible()
        binding.errorImageView.invisible()
        binding.errorTextView.invisible()
        binding.textView.text = getString(R.string.initializing)
    }

    private fun showErrorView(errorMsg: String) {
        binding.textView.invisible()
        binding.progressIndicator.invisible()
        binding.errorImageView.visible()
        binding.errorTextView.visible()
        binding.errorTextView.crossFadeSetText(getString(R.string.initialize_error, errorMsg))
    }

    private fun runAfterInitialized() {
        startActivity(Intent(this, MenuConfigManager.readMenuConfig().firstActivityClass))
        finish()
    }

    private fun navigateToSetup() {
        start<SetupActivity>()
        finish()
    }
}