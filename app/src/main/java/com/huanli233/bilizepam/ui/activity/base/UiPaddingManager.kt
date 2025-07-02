package com.huanli233.bilizepam.ui.activity.base

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager
import com.huanli233.bilizepam.data.setting.LocalData

class UiPaddingManager(
    private val context: Context
) {
    var windowWidth: Int = 0
        private set
    var windowHeight: Int = 0
        private set

    fun applyRootViewPadding(rootView: View) {
        val paddingHPercent: Int = LocalData.settings.uiSettings.uiPaddingHorizontal
        val paddingVPercent: Int = LocalData.settings.uiSettings.uiPaddingVertical

        val windowManager: WindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display = if (Build.VERSION.SDK_INT >= 30) {
            context.display
        } else {
            @Suppress("DEPRECATION") windowManager.defaultDisplay
        }
        val screenWidth: Int
        val screenHeight: Int
        if (Build.VERSION.SDK_INT >= 30) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics)
            else display.getMetrics(metrics)

            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }

        if (paddingHPercent != 0 || paddingVPercent != 0) {
            val paddingHorizontal: Int = screenWidth * paddingHPercent / 100
            val paddingTop: Int = screenHeight * paddingVPercent / 100
            val paddingBottom = if (LocalData.settings.uiSettings.roundMode) {
                (paddingTop + screenHeight * 0.03).toInt()
            } else {
                paddingTop
            }

            windowWidth = screenWidth - paddingHorizontal - paddingHorizontal
            windowHeight = screenHeight - paddingTop - (paddingBottom - paddingTop)
            rootView.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom)
        } else {
            windowWidth = screenWidth
            windowHeight = screenHeight
        }
    }
}