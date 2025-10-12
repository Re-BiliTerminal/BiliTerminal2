package com.huanli233.bilizepam.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.navigation.AppNavHost
import com.huanli233.bilizepam.ui.theme.BiliZepamTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class MainActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BiliZepamTheme {
                AppNavHost()
            }
        }
    }

}