package com.mitron.connect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme

/** Mirrors the wireframe's circular initials `.avatar`. */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = AvatarSize.small,
    background: Color = ConnectTheme.colors.accentBg,
    foreground: Color = ConnectTheme.colors.accent,
    fontSize: TextUnit = (size.value / 2.8f).sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, shape = androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = foreground,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

fun String?.toImageModel(): Any? {
    if (this == null) return null
    if (this.startsWith("data:image")) {
        try {
            val base64Str = this.substringAfter("base64,")
            val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
            return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return this
}
