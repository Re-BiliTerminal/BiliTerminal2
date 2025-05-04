package com.huanli233.biliterminal2.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.setup.SetupActivity
import kotlinx.coroutines.launch

class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BiliTerminal2)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}