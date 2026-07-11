package com.mitron.connect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme(
    primary           = BrandBlue,
    onPrimary         = OnBrandBlue,
    primaryContainer  = AccentTealBgLight,
    onPrimaryContainer= AccentTealLight,
    secondary         = AccentTealLight,
    onSecondary       = SurfaceLight1,
    tertiary          = AiIndigo,
    onTertiary        = SurfaceLight1,
    error             = DangerRed,
    onError           = SurfaceLight1,
    background        = SurfaceLight2,
    onBackground      = TextPrimaryLight,
    surface           = SurfaceLight1,
    onSurface         = TextPrimaryLight,
    surfaceVariant    = SurfaceLight3,
    onSurfaceVariant  = TextSecondaryLight,
    outline           = BorderStrongLight,
    outlineVariant    = BorderLight,
)

private val DarkScheme = darkColorScheme(
    primary           = BrandBlue,
    onPrimary         = OnBrandBlue,
    primaryContainer  = AccentTealBgDark,
    onPrimaryContainer= AccentTealDark,
    secondary         = AccentTealDark,
    onSecondary       = SurfaceDark1,
    tertiary          = AiIndigo,
    onTertiary        = SurfaceDark1,
    error             = DangerRed,
    onError           = SurfaceDark1,
    background        = SurfaceDark1,
    onBackground      = TextPrimaryDark,
    surface           = SurfaceDark2,
    onSurface         = TextPrimaryDark,
    surfaceVariant    = SurfaceDark3,
    onSurfaceVariant  = TextSecondaryDark,
    outline           = BorderStrongDark,
    outlineVariant    = BorderDark,
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
    val colorScheme   = if (darkTheme) DarkScheme else LightScheme
    val connectColors = if (darkTheme) DarkConnectColors else LightConnectColors

    CompositionLocalProvider(LocalConnectColors provides connectColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = ConnectTypography,
            shapes      = ConnectShapes,
            content     = content,
        )
    }
}
