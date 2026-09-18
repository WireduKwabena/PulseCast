package com.masterminds.pulsecast.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * "Broadcast Obsidian" is explicitly an OLED-optimized dark palette per
 * DESIGN.md. This is a broadcaster/gamer capture tool meant to
 * recede into pure black.
 */
private val PulseCastColorScheme = darkColorScheme(
    background = BgBase,
    surface = SurfaceLow,
    surfaceVariant = SurfaceVariant,
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    error = Error,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = StrokeSubtle,
)

/**
 * Minimal Material3 Typography for base component interop.
 * Actual UI code should reference PulseCastType's exact named styles directly.
 */
private val PulseCastMaterialTypography = Typography(
    bodyLarge = PulseCastType.bodyLg,
    bodyMedium = PulseCastType.bodyMd,
    bodySmall = PulseCastType.bodySm,
    headlineLarge = PulseCastType.headlineXl,
    headlineMedium = PulseCastType.headlineLg,
    headlineSmall = PulseCastType.headlineSm,
    labelLarge = PulseCastType.buttonText,
)

@Composable
fun PulseCastTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = BgBase.toArgb()
            window.navigationBarColor = BgBase.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PulseCastColorScheme,
        typography = PulseCastMaterialTypography,
        content = content,
    )
}
