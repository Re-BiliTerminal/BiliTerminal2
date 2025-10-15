package com.huanli233.bilizepam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.huanli233.bilizepam.data.proto.NightMode
import com.huanli233.bilizepam.data.setting.LocalData
import com.materialkolor.rememberDynamicColorScheme
import androidx.wear.compose.material3.ColorScheme as WearColorScheme

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun BiliZepamTheme(
    darkTheme: Boolean = when (LocalData.settings.theme.nightMode) {
        NightMode.NIGHT_MODE_DAY -> false
        NightMode.NIGHT_MODE_NIGHT -> true
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val useSystemAccent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && LocalData.settings.theme.followSystemAccent

    val selectedColorThemeKey = LocalData.settings.theme.colorTheme

    val colorScheme = if (useSystemAccent) {
        // Use system dynamic colors
        if (darkTheme) DarkColors else LightColors
    } else {
        val seedColor = AppSeedColors.colorMap[selectedColorThemeKey] ?: AppSeedColors.MATERIAL_PURPLE
        rememberDynamicColorScheme(seedColor = seedColor, isDark = darkTheme)
    }

    val wearColorScheme = colorScheme.toWearColorScheme()

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme
    ) {
        androidx.wear.compose.material3.MaterialTheme(
            colorScheme = wearColorScheme,
            content = {
                CompositionLocalProvider(
                    LocalContentColor provides colorScheme.onSurface
                ) {
                    content()
                }
            }
        )
    }
}

private fun androidx.compose.material3.ColorScheme.toWearColorScheme(): WearColorScheme {
    return WearColorScheme(
        primary = primary,
        primaryDim = primaryContainer,
        primaryContainer = primaryContainer,
        onPrimary = onPrimary,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        secondaryDim = secondaryContainer,
        secondaryContainer = secondaryContainer,
        onSecondary = onSecondary,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        tertiaryDim = tertiaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiary = onTertiary,
        onTertiaryContainer = onTertiaryContainer,
        surfaceContainerLow = surfaceVariant,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = surface,
        onSurface = onSurface,
        onSurfaceVariant = onSurfaceVariant,
        background = background,
        onBackground = onBackground,
        error = error,
        errorDim = error,
        errorContainer = errorContainer,
        onError = onError,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
    )
}