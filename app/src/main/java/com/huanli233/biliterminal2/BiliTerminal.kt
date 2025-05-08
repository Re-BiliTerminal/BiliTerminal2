package com.huanli233.biliterminal2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.google.android.material.color.DynamicColors
import com.huanli233.biliterminal2.data.setting.DataStore
import com.huanli233.biliterminal2.data.setting.toSystemValue
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltAndroidApp
class BiliTerminal : Application() {

    init {
        application = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        contextNullable = base
        MultiDex.install(this)
    }

    private var lastThemeMode: Int = -1

    override fun onCreate() {
        super.onCreate()
        XLog.init(
            if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR,
            AndroidPrinter()
        )
        contextNullable = applicationContext
        DynamicColors.applyToActivitiesIfAvailable(this)
        lastThemeMode = DataStore.appSettings.nightMode.toSystemValue()
        setDefaultNightMode(lastThemeMode)
        applicationScope.launch {
            DataStore.appSettingsStateFlow.collectLatest { config ->
                config?.let {
                    if (lastThemeMode != it.nightMode.toSystemValue()) {
                        lastThemeMode = it.nightMode.toSystemValue()
                        withContext(Dispatchers.Main) {
                            setDefaultNightMode(lastThemeMode)
                        }
                    }
                }
            }
        }
        ErrorCatcher.instance.install(applicationContext)
    }

    companion object {
        lateinit var application: BiliTerminal
            private set

        fun setDefaultNightMode(mode: Int) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }

    }
}

val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@JvmField
@SuppressLint("StaticFieldLeak")
var contextNullable: Context? = null

val applicationContext: Context
    get() = contextNullable!!
