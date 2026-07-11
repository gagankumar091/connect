package com.mitron.connect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ConnectButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Filled,
    enabled: Boolean = true
) {
    when (variant) {
        ButtonVariant.Filled -> Button(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
        ButtonVariant.Tonal -> FilledTonalButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
        ButtonVariant.Outlined -> OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
    }
}

enum class ButtonVariant { Filled, Tonal, Outlined }

@Composable
fun ConnectTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ContactCard(
    name: String,
    company: String,
    avatarUrl: String?,
    score: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl ?: "https://api.dicebear.com/7.x/initials/svg?seed=$name",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Badge(
                containerColor = if (score > 80) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (score > 80) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = score.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun EventCard(
    title: String,
    date: String,
    location: String,
    imageUrl: String?,
    isAttending: Boolean = false,
    attendeeCount: Int = 0,
    onRsvpClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")
    val cardBgColor by animateColorAsState(
        targetValue = if (isAttending) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "bgColor"
    )

    OutlinedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth().graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        colors = CardDefaults.outlinedCardColors(containerColor = cardBgColor)
    ) {
        Column {
            AsyncImage(
                model = imageUrl ?: "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$attendeeCount Attending", style = MaterialTheme.typography.labelSmall)
                }
                
                Button(
                    onClick = onRsvpClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAttending) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isAttending) "Going" else "Interested")
                }
            }
        }
    }
}
