package com.huanli233.biliterminal2.ui.activity

import android.os.Bundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.main.MainActivity
import splitties.activities.start

class SplashActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_BiliTerminal2)

        start<MainActivity>()
        finish()
    }
}