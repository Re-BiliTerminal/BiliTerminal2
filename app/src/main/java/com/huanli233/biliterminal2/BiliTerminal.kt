package com.huanli233.biliterminal2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.google.android.material.color.DynamicColors
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.data.setting.toSystemValue
import com.huanli233.biliterminal2.utils.locale.LocaleDelegate
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
        if (LocalData.settings.theme.followSystemAccent) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        LocaleDelegate.defaultLocale = getLocale()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            @Suppress("DEPRECATION") resources.updateConfiguration(resources.configuration.apply {
                setLocale(LocaleDelegate.defaultLocale)
            }, resources.displayMetrics)
        }
        lastThemeMode = LocalData.settings.theme.nightMode.toSystemValue()
        setDefaultNightMode(lastThemeMode)
        applicationScope.launch {
            LocalData.settingsStateFlow.collectLatest { config ->
                config?.let {
                    if (lastThemeMode != it.theme.nightMode.toSystemValue()) {
                        lastThemeMode = it.theme.nightMode.toSystemValue()
                        withContext(Dispatchers.Main) {
                            setDefaultNightMode(lastThemeMode)
                        }
                    }
                }
            }
        }
        ErrorCatcher.instance.install(applicationContext)
    }

    fun getLocale(tag: String): Locale {
        if (tag.isEmpty() || "SYSTEM" == tag || Build.VERSION.SDK_INT < 21) {
            return LocaleDelegate.systemLocale
        }
        return Locale.forLanguageTag(tag)
    }

    fun getLocale(): Locale {
        val tag = LocalData.settings.language
        return getLocale(tag)
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
