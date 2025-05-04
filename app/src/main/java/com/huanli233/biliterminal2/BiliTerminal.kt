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
import com.huanli233.biliterminal2.data.NIGHT_MODE_DISABLED
import com.huanli233.biliterminal2.data.NIGHT_MODE_FOLLOW_SYSTEM
import com.huanli233.biliterminal2.data.UserPreferences
import com.huanli233.biliterminal2.utils.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BiliTerminal : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        contextNullable = base
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        XLog.init(
            if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR,
            AndroidPrinter()
        )
        contextNullable = getFitDisplayContext(this@BiliTerminal.applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(this)
        setDefaultNightMode()
        Preferences.sharedPreferences = getSharedPreferences("default", MODE_PRIVATE)
        ErrorCatch.instance.install(applicationContext)
    }

    companion object {
        fun setDefaultNightMode() {
            AppCompatDelegate.setDefaultNightMode(
                when (UserPreferences.nightMode.get()) {
                    NIGHT_MODE_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    NIGHT_MODE_DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }

        fun getFitDisplayContext(old: Context): Context? {
            val dpiTimes = UserPreferences.uiScale.get()
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
