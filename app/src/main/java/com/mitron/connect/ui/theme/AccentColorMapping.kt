package com.mitron.connect.ui.theme

import androidx.compose.ui.graphics.Color
import com.mitron.connect.data.model.AccentColor

/** Maps the data layer's semantic [AccentColor] to a (background, foreground) tint pair. */
fun AccentColor.tints(colors: ConnectColorScheme): Pair<Color, Color> = when (this) {
    AccentColor.ACCENT -> colors.accentBg to colors.accent
    AccentColor.PRO -> colors.proBg to colors.pro
    AccentColor.SUCCESS -> colors.successBg to colors.success
    AccentColor.DANGER -> colors.dangerBg to colors.danger
    AccentColor.NEUTRAL -> colors.surface2 to colors.textSecondary
}
