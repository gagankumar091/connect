package com.mitron.connect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mitron.connect.ui.theme.ConnectTheme

/** Mirrors the wireframe's `.btn-p` solid primary CTA. */
@Composable
fun ConnectPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = ConnectTheme.colors.fillPrimary,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = ConnectTheme.colors.onPrimary,
        ),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Mirrors the wireframe's `.btn-s` outlined secondary action. */
@Composable
fun ConnectSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit = {},
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = if (compact) {
            androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        } else {
            ButtonDefaults.ContentPadding
        },
        border = BorderStroke(0.5.dp, ConnectTheme.colors.borderStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ConnectTheme.colors.textPrimary),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
    }
}
