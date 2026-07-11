package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.Event
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

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
        modifier = modifier.fillMaxSize().background(colors.surface1),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            if (hasLocationPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.pro, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Showing live nearby events based on your location", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Share location to see live nearby events.", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
        }
        
        items(events) { event ->
            EventRow(event = event, onClick = { onOpenEvent(event.id) })
            Divider(
                color = colors.border,
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 76.dp)
            )
        }
    }
}

@Composable
private fun EventRow(event: Event, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accentBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Event, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
        }
        
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(
                event.title, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${event.location} • ${event.date}", 
                style = MaterialTheme.typography.bodyMedium, 
                color = colors.textSecondary
            )
        }
        
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}
