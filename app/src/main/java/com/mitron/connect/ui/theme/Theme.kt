package com.mitron.connect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = OnBrandRed,
    secondary = AccentTealLight,
    onSecondary = SurfaceLight1,
    tertiary = AiPurple,
    onTertiary = SurfaceLight1,
    error = DangerRed,
    onError = SurfaceLight1,
    background = SurfaceLight1,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight1,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight2,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderStrongLight,
    outlineVariant = BorderLight,
)

private val DarkScheme = darkColorScheme(
    primary = BrandRed,
    onPrimary = OnBrandRed,
    secondary = AccentTealDark,
    onSecondary = SurfaceDark1,
    tertiary = AiPurple,
    onTertiary = SurfaceDark1,
    error = DangerRed,
    onError = SurfaceDark1,
    background = SurfaceDark1,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark1,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark2,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderStrongDark,
    outlineVariant = BorderDark,
)

object ConnectTheme {
    val colors: ConnectColorScheme
        @Composable get() = LocalConnectColors.current
}

@Composable
fun ConnectAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val connectColors = if (darkTheme) DarkConnectColors else LightConnectColors

    CompositionLocalProvider(LocalConnectColors provides connectColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ConnectTypography,
            shapes = ConnectShapes,
            content = content,
        )
    }
}
