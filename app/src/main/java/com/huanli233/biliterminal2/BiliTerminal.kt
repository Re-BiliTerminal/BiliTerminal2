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
        contextNullable = getFitDisplayContext(this@BiliTerminal.applicationContext)
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

        fun getFitDisplayContext(old: Context): Context? {
            val dpiTimes = DataStore.appSettings.uiScale
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return old
            if (dpiTimes == 1.0f) return old
            try {
                val displayMetrics = old.resources.displayMetrics
                val configuration = old.resources.configuration
                configuration.densityDpi = (displayMetrics.densityDpi * dpiTimes).toInt()
                return old.createConfigurationContext(configuration)
            } catch (_: Exception) {
                return old
            }
        }
    }
}

val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@JvmField
@SuppressLint("StaticFieldLeak")
var contextNullable: Context? = null

val applicationContext: Context
    get() = contextNullable!!
