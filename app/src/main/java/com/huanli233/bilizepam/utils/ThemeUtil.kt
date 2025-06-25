package com.huanli233.bilizepam.utils

import androidx.annotation.StyleRes
import com.google.android.material.color.DynamicColors
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData

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

    val colorTextMap = mapOf(
        "SAKURA" to R.string.color_sakura,
        "MATERIAL_RED" to R.string.color_red,
        "MATERIAL_PINK" to R.string.color_pink,
        "MATERIAL_PURPLE" to R.string.color_purple,
        "MATERIAL_DEEP_PURPLE" to R.string.color_deep_purple,
        "MATERIAL_INDIGO" to R.string.color_indigo,
        "MATERIAL_BLUE" to R.string.color_blue,
        "MATERIAL_LIGHT_BLUE" to R.string.color_light_blue,
        "MATERIAL_CYAN" to R.string.color_cyan,
        "MATERIAL_TEAL" to R.string.color_teal,
        "MATERIAL_GREEN" to R.string.color_green,
        "MATERIAL_LIGHT_GREEN" to R.string.color_light_green,
        "MATERIAL_LIME" to R.string.color_lime,
        "MATERIAL_YELLOW" to R.string.color_yellow,
        "MATERIAL_AMBER" to R.string.color_amber,
        "MATERIAL_ORANGE" to R.string.color_orange,
        "MATERIAL_DEEP_ORANGE" to R.string.color_deep_orange,
        "MATERIAL_BROWN" to R.string.color_brown,
        "MATERIAL_BLUE_GREY" to R.string.color_blue_grey
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