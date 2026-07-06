package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.Event
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventsTabContent(
    modifier: Modifier = Modifier,
    events: List<Event>,
    contacts: List<Contact>,
    connectionStatuses: Map<String, String>,
    hasLocationPermission: Boolean,
    onOpenProfile: (String) -> Unit,
    onConnect: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenEvent: (String) -> Unit
) {
    val colors = ConnectTheme.colors

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            if (hasLocationPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(colors.surface2, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).padding(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.pro, modifier = Modifier.padding(end = Spacing.xs))
                    Text("Showing live nearby events based on your location", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                }
            } else {
                Text("Share location to see live nearby events.", style = MaterialTheme.typography.bodySmall, color = colors.textMuted, modifier = Modifier.padding(bottom = Spacing.sm))
            }
        }
        
        items(events) { event ->
            ConnectCard(modifier = Modifier.clickable { onOpenEvent(event.id) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = Spacing.xs)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.accent, modifier = Modifier.padding(end = Spacing.xs))
                        Text(event.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    }
                    
                    Text(
                        text = "${event.location} • ${event.date}", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = colors.textMuted,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )
                    
                    Text("Tap to view details & attendees", style = MaterialTheme.typography.labelSmall, color = colors.fillPrimary)
                }
            }
        }
    }
}

@Composable
private fun EventPersonRow(person: Contact, status: String?, onClick: () -> Unit, onConnect: () -> Unit, onChat: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = person.color.tints(colors)
    ConnectCard {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
            Avatar(initials = person.initials, size = AvatarSize.small, background = bg, foreground = fg)
            Column(modifier = Modifier.padding(start = Spacing.xs).weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(person.title ?: "Attendee", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            Row(modifier = Modifier.width(80.dp), horizontalArrangement = Arrangement.End) {
                if (status == "ACCEPTED") {
                    ConnectSecondaryButton(
                        text = "Chat",
                        compact = true,
                        modifier = Modifier.width(80.dp),
                        onClick = onChat
                    )
                } else if (status == "SENT") {
                    ConnectSecondaryButton(
                        text = "Sent",
                        compact = true,
                        modifier = Modifier.width(80.dp),
                        onClick = { }
                    )
                } else {
                    ConnectSecondaryButton(
                        text = "Connect",
                        compact = true,
                        modifier = Modifier.width(80.dp),
                        onClick = onConnect
                    )
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun EventsTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        EventsTabContent(
            events = emptyList(),
            contacts = emptyList(),
            connectionStatuses = emptyMap(),
            hasLocationPermission = true,
            onOpenProfile = {},
            onConnect = {},
            onOpenChat = {},
            onOpenEvent = {}
        )
    }
}
