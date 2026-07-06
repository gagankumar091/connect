package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.ConnectCard
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    viewModel: EventDetailViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    
    val event by viewModel.event.collectAsState()
    val attendees by viewModel.attendees.collectAsState()
    val connectionStatuses by viewModel.connectionStatuses.collectAsState()
    
    if (event == null) {
        return
    }

    Scaffold(topBar = { ConnectTopBar(title = "Event Details", onBack = onBack) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(Spacing.md).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).background(colors.surface2, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = colors.fillPrimary, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.padding(start = Spacing.sm)) {
                        Text(event!!.title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                    }
                }
            }

            item {
                ConnectCard(modifier = Modifier.padding(top = Spacing.xs)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        if (!event!!.date.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                                Text(event!!.date!!, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, modifier = Modifier.padding(start = Spacing.xs))
                            }
                        }
                        if (!event!!.location.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                                Text(event!!.location!!, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, modifier = Modifier.padding(start = Spacing.xs))
                            }
                        }
                    }
                }
            }

            if (!event!!.description.isNullOrEmpty()) {
                item {
                    Text("Description", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.padding(top = Spacing.sm))
                    Text(event!!.description!!, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }

            item {
                Text("People Attending", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs))
            }

            items(attendees) { contact ->
                EventAttendeeRow(
                    contact = contact,
                    status = connectionStatuses[contact.id],
                    onConnect = { viewModel.connectWithContact(contact.id) },
                    onProfile = { onOpenProfile(contact.id) }
                )
            }
        }
    }
}

@Composable
private fun EventAttendeeRow(contact: Contact, status: String?, onConnect: () -> Unit, onProfile: () -> Unit) {
    val colors = ConnectTheme.colors
    ConnectCard(modifier = Modifier.clickable(onClick = onProfile)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Avatar(initials = contact.initials, size = AvatarSize.small)
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(contact.title ?: "Mitron User", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
            if (status == "SENT") {
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
