package com.mitron.connect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.PillShape

enum class TagStyle { NEUTRAL, ACCENT, PRO, SUCCESS, DANGER, SOLID }

/** Mirrors the wireframe's `.tag` pill chip. */
@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    style: TagStyle = TagStyle.NEUTRAL,
) {
    val colors = ConnectTheme.colors
    val (bg, fg) = when (style) {
        TagStyle.NEUTRAL -> colors.surface2 to colors.textSecondary
        TagStyle.ACCENT -> colors.accentBg to colors.accent
        TagStyle.PRO -> colors.proBg to colors.pro
        TagStyle.SUCCESS -> colors.successBg to colors.success
        TagStyle.DANGER -> colors.dangerBg to colors.danger
        TagStyle.SOLID -> colors.textPrimary to colors.surface1
    }
    Text(
        text = text,
        color = fg,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(bg, PillShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
