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

package com.huanli233.biliterminal2.utils.locale

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils

import java.util.Locale
import androidx.core.text.layoutDirection

class LocaleDelegate {

    /** locale of this instance  */
    private var locale = Locale.getDefault()

    /**
     * Return if current locale is different from default.
     *
     * Call this in [Activity.onResume] and if true you should recreate activity.
     *
     * @return locale changed
     */
    val isLocaleChanged: Boolean
        get() = defaultLocale != locale

    /**
     * Update locale of given configuration, call in [Activity.attachBaseContext].
     *
     * @param configuration Configuration
     */
    fun updateConfiguration(configuration: Configuration) {
        locale = defaultLocale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        }
    }

    /**
     * A dirty fix for wrong layout direction after switching locale between LTR and RLT language,
     * call in [Activity.onCreate].
     *
     * @param activity Activity
     */
    fun onCreate(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.window.decorView.layoutDirection = locale.layoutDirection
        }
    }

    companion object {

        /** current locale  */
        @JvmStatic
        var defaultLocale: Locale = Locale.getDefault()

        /** system locale  */
        @JvmStatic
        var systemLocale: Locale = Locale.getDefault()
    }
}