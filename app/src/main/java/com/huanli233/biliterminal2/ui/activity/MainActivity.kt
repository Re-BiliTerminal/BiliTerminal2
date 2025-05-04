package com.huanli233.biliterminal2.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.setup.SetupActivity
import kotlinx.coroutines.launch

class MainActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (UserPreferences.firstRun.get()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
        } else {
            // TODO check cookie and network
        }
    }
}