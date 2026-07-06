package com.mitron.connect.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Semantic color slots mirroring the hifi wireframe's CSS custom properties
 * (--surface-1, --bg-pro, --text-accent, etc). Material3's ColorScheme only
 * covers primary/secondary/tertiary/error, so this supplements it with the
 * extra "accent" and "pro" (AI) slots the wireframe relies on.
 */
data class ConnectColorScheme(
    val surface1: androidx.compose.ui.graphics.Color,
    val surface2: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textMuted: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val borderStrong: androidx.compose.ui.graphics.Color,
    val borderStronger: androidx.compose.ui.graphics.Color,
    val fillPrimary: androidx.compose.ui.graphics.Color,
    val onPrimary: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val accentBg: androidx.compose.ui.graphics.Color,
    val pro: androidx.compose.ui.graphics.Color,
    val proBg: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val successBg: androidx.compose.ui.graphics.Color,
    val danger: androidx.compose.ui.graphics.Color,
    val dangerBg: androidx.compose.ui.graphics.Color,
)

val LightConnectColors = ConnectColorScheme(
    surface1 = SurfaceLight1,
    surface2 = SurfaceLight2,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    border = BorderLight,
    borderStrong = BorderStrongLight,
    borderStronger = BorderStrongerLight,
    fillPrimary = BrandRed,
    onPrimary = OnBrandRed,
    accent = AccentTealLight,
    accentBg = AccentTealBgLight,
    pro = AiPurple,
    proBg = AiPurpleBgLight,
    success = SuccessGreen,
    successBg = SuccessBgLight,
    danger = DangerRed,
    dangerBg = DangerBgLight,
)

val DarkConnectColors = ConnectColorScheme(
    surface1 = SurfaceDark1,
    surface2 = SurfaceDark2,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    border = BorderDark,
    borderStrong = BorderStrongDark,
    borderStronger = BorderStrongerDark,
    fillPrimary = BrandRed,
    onPrimary = OnBrandRed,
    accent = AccentTealDark,
    accentBg = AccentTealBgDark,
    pro = AiPurple,
    proBg = AiPurpleBgDark,
    success = SuccessGreen,
    successBg = SuccessBgDark,
    danger = DangerRed,
    dangerBg = DangerBgDark,
)

val LocalConnectColors = staticCompositionLocalOf { LightConnectColors }
