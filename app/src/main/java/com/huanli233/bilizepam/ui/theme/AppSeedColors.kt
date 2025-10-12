package com.huanli233.bilizepam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.huanli233.bilizepam.data.setting.LocalData
import com.materialkolor.DynamicMaterialTheme

object AppSeedColors {
    val SAKURA = Color(0xFFF8BBD0)
    val MATERIAL_RED = Color(0xFFF44336)
    val MATERIAL_PINK = Color(0xFFE91E63)
    val MATERIAL_PURPLE = Color(0xFF9C27B0)
    val MATERIAL_DEEP_PURPLE = Color(0xFF673AB7)
    val MATERIAL_INDIGO = Color(0xFF3F51B5)
    val MATERIAL_BLUE = Color(0xFF2196F3)
    val MATERIAL_LIGHT_BLUE = Color(0xFF03A9F4)
    val MATERIAL_CYAN = Color(0xFF00BCD4)
    val MATERIAL_TEAL = Color(0xFF009688)
    val MATERIAL_GREEN = Color(0xFF4CAF50)
    val MATERIAL_LIGHT_GREEN = Color(0xFF8BC34A)
    val MATERIAL_LIME = Color(0xFFCDDC39)
    val MATERIAL_YELLOW = Color(0xFFFFEB3B)
    val MATERIAL_AMBER = Color(0xFFFFC107)
    val MATERIAL_ORANGE = Color(0xFFFF9800)
    val MATERIAL_DEEP_ORANGE = Color(0xFFFF5722)
    val MATERIAL_BROWN = Color(0xFF795548)
    val MATERIAL_BLUE_GREY = Color(0xFF607D8B)

    val colorMap: Map<String, Color> = mapOf(
        "SAKURA" to SAKURA,
        "MATERIAL_RED" to MATERIAL_RED,
        "MATERIAL_PINK" to MATERIAL_PINK,
        "MATERIAL_PURPLE" to MATERIAL_PURPLE,
        "MATERIAL_DEEP_PURPLE" to MATERIAL_DEEP_PURPLE,
        "MATERIAL_INDIGO" to MATERIAL_INDIGO,
        "MATERIAL_BLUE" to MATERIAL_BLUE,
        "MATERIAL_LIGHT_BLUE" to MATERIAL_LIGHT_BLUE,
        "MATERIAL_CYAN" to MATERIAL_CYAN,
        "MATERIAL_TEAL" to MATERIAL_TEAL,
        "MATERIAL_GREEN" to MATERIAL_GREEN,
        "MATERIAL_LIGHT_GREEN" to MATERIAL_LIGHT_GREEN,
        "MATERIAL_LIME" to MATERIAL_LIME,
        "MATERIAL_YELLOW" to MATERIAL_YELLOW,
        "MATERIAL_AMBER" to MATERIAL_AMBER,
        "MATERIAL_ORANGE" to MATERIAL_ORANGE,
        "MATERIAL_DEEP_ORANGE" to MATERIAL_DEEP_ORANGE,
        "MATERIAL_BROWN" to MATERIAL_BROWN,
        "MATERIAL_BLUE_GREY" to MATERIAL_BLUE_GREY
    )
}
