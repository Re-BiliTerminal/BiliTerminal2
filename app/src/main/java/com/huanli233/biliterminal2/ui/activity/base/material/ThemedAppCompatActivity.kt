/**
 * Copyright (c) 2017 Rikka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.huanli233.biliterminal2.ui.activity.base.material

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.huanli233.biliterminal2.utils.locale.LocaleDelegate

open class ThemedAppCompatActivity: AppCompatActivity() {

    private val localeDelegate by lazy {
        LocaleDelegate()
    }

    private var userThemeKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        localeDelegate.onCreate(this)
        resetTitle()
        fixWindowFlags()

        userThemeKey = computeUserThemeKey()

        super.onCreate(savedInstanceState)

        onApplyUserThemeResourceForDecorView()
    }

    open fun computeUserThemeKey(): String? {
        return null
    }

    open fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
    }

    private fun onApplyUserThemeResourceForDecorView() {
        // apply style to DecorContext to correct theme of PopupWindow, etc.
        if (window?.decorView?.context?.theme != null) {
            onApplyUserThemeResource(window.decorView.context.theme!!, true)
        }
    }

    @Suppress("DEPRECATION")
    override fun onApplyThemeResource(theme: Resources.Theme, resid: Int, first: Boolean) {
        // apply real style and our custom style
        if (parent == null) {
            theme.applyStyle(resid, true)
        } else {
            try {
                theme.setTo(parent.theme)
            } catch (_: Exception) {
                // Empty
            }

            theme.applyStyle(resid, false)
        }

        onApplyUserThemeResource(theme, false)

        // only pass theme style to super, so styled theme will not be overwritten
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onApplyThemeResource(theme, android.R.style.ThemeOverlay, first)
        } else {
            super.onApplyThemeResource(theme, resid, first)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        if (localeDelegate.isLocaleChanged
                || userThemeKey != computeUserThemeKey()) {
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        localeDelegate.updateConfiguration(configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            super.attachBaseContext(newBase.createConfigurationContext(configuration))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    private fun resetTitle() {
        var label = try {
            packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
        } catch (_: PackageManager.NameNotFoundException) {
            0
        }
        if (label == 0) {
            label = applicationInfo.labelRes
        }
        if (label != 0) {
            setTitle(label)
        }
    }

    /**
     * Fix windowLightStatusBar not changed after applyStyle on Android O
     */
    @Suppress("DEPRECATION")
    private fun fixWindowFlags() {
        var a: TypedArray
        var flag = window.decorView.systemUiVisibility

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            a = obtainStyledAttributes(intArrayOf(android.R.attr.windowLightStatusBar))
            val windowLightStatusBar = a.getBoolean(0, false)
            a.recycle()

            flag = if (windowLightStatusBar) {
                flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flag and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            a = obtainStyledAttributes(intArrayOf(android.R.attr.windowLightNavigationBar))
            val windowLightNavigationBar = a.getBoolean(0, false)
            a.recycle()

            flag = if (windowLightNavigationBar) {
                flag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flag and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }

        window.decorView.systemUiVisibility = flag
    }
}