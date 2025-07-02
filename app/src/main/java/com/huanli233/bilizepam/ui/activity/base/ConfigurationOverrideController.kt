package com.huanli233.bilizepam.ui.activity.base

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import com.huanli233.bilizepam.data.setting.LocalData
import kotlin.ranges.contains

class ConfigurationOverrideController(
    private val context: Context
) {
    private lateinit var _originalContext: Context
    private var _lastOriginalViewContext: Context? = null
    private var _configurationChanged = false
    val originalViewContext: Context
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _lastOriginalViewContext?.let {
                if (!_configurationChanged) {
                    it
                } else {
                    null
                }
            } ?: let {
                _configurationChanged = false
                overrideToSystemConfiguration()
            }
        } else {
            context
        }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun overrideToSystemConfiguration(): Context {
        return object : ContextWrapper(context) {
            val mResources = baseContext.resources.run {
                val system = Resources.getSystem()
                @Suppress("DEPRECATION") Resources(
                    assets,
                    DisplayMetrics().apply {
                        setTo(system.displayMetrics)
                    },
                    Configuration(configuration).apply {
                        densityDpi = system.configuration.densityDpi
                    }
                )
            }

            override fun getResources(): Resources? {
                return mResources
            }
        }.also {
            _lastOriginalViewContext = it
        }
    }

    fun overrideConfiguration(baseContext: Context): Context {
        val dpiTimes = LocalData.settings.uiSettings.uiScale
        val density = LocalData.settings.uiSettings.density
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return baseContext
        return runCatching {
            val configuration = baseContext.resources.configuration
            if (density >= 72) {
                configuration.densityDpi = density
                configuration.fontScale = 1.0f
                baseContext.createConfigurationContext(configuration)
            } else if (dpiTimes in 0.25..5.0) {
                val displayMetrics = baseContext.resources.displayMetrics
                configuration.densityDpi = (displayMetrics.densityDpi * dpiTimes).toInt()
                baseContext.createConfigurationContext(configuration)
            } else {
                baseContext
            }
        }.getOrNull() ?: baseContext
    }

    fun attachContext(base: Context) {
        _originalContext = base
    }

    fun configurationChanged() {
        _configurationChanged = true
    }
}