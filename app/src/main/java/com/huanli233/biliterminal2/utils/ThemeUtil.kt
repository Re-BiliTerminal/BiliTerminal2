package com.huanli233.biliterminal2.utils

import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData

object ThemeUtil {

    val colorThemeMap: Map<String, Int> = mapOf(
        "SAKURA" to R.style.ThemeOverlay_MaterialSakura,
        "MATERIAL_RED" to R.style.ThemeOverlay_MaterialRed,
        "MATERIAL_PINK" to R.style.ThemeOverlay_MaterialPink,
        "MATERIAL_PURPLE" to R.style.ThemeOverlay_MaterialPurple,
        "MATERIAL_DEEP_PURPLE" to R.style.ThemeOverlay_MaterialDeepPurple,
        "MATERIAL_INDIGO" to R.style.ThemeOverlay_MaterialIndigo,
        "MATERIAL_BLUE" to R.style.ThemeOverlay_MaterialBlue,
        "MATERIAL_LIGHT_BLUE" to R.style.ThemeOverlay_MaterialLightBlue,
        "MATERIAL_CYAN" to R.style.ThemeOverlay_MaterialCyan,
        "MATERIAL_TEAL" to R.style.ThemeOverlay_MaterialTeal,
        "MATERIAL_GREEN" to R.style.ThemeOverlay_MaterialGreen,
        "MATERIAL_LIGHT_GREEN" to R.style.ThemeOverlay_MaterialLightGreen,
        "MATERIAL_LIME" to R.style.ThemeOverlay_MaterialLime,
        "MATERIAL_YELLOW" to R.style.ThemeOverlay_MaterialYellow,
        "MATERIAL_AMBER" to R.style.ThemeOverlay_MaterialAmber,
        "MATERIAL_ORANGE" to R.style.ThemeOverlay_MaterialOrange,
        "MATERIAL_DEEP_ORANGE" to R.style.ThemeOverlay_MaterialDeepOrange,
        "MATERIAL_BROWN" to R.style.ThemeOverlay_MaterialBrown,
        "MATERIAL_BLUE_GREY" to R.style.ThemeOverlay_MaterialBlueGrey
    )

    const val THEME_DEFAULT: String = "DEFAULT"

    fun isSystemAccent(): Boolean {
        return DynamicColors.isDynamicColorAvailable() && LocalData.settings.theme.followSystemAccent
    }

    fun getColorTheme(): String {
        if (isSystemAccent()) {
            return "SYSTEM"
        }
        return LocalData.settings.theme.colorTheme
    }

    @StyleRes
    fun getColorThemeStyleRes(): Int {
        val theme = colorThemeMap[getColorTheme()]
        if (theme == null) {
            return R.style.ThemeOverlay_MaterialPurple
        }
        return theme
    }

}