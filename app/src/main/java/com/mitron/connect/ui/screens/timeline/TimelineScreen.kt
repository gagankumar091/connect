package com.mitron.connect.ui.screens.timeline

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.TimelineEvent
import com.mitron.connect.data.model.TimelineIcon
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

private fun TimelineIcon.imageVector(): ImageVector = when (this) {
    TimelineIcon.LOCATION -> Icons.Filled.LocationOn
    TimelineIcon.QR -> Icons.Filled.QrCode
    TimelineIcon.FILE -> Icons.Filled.Description
    TimelineIcon.COFFEE -> Icons.Filled.Coffee
    TimelineIcon.SEND -> Icons.Filled.Send
    TimelineIcon.CHECK -> Icons.Filled.Check
}

@Composable
fun TimelineScreen(
    contactId: String,
    onBack: () -> Unit,
    onOpenMeetingSummary: (String) -> Unit,
    viewModel: TimelineViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    
    androidx.compose.runtime.LaunchedEffect(contactId) {
        viewModel.loadTimeline(contactId)
    }
    
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log a Meeting", color = colors.textPrimary) },
            text = {
                Column {
                    Text("Enter rough notes. The AI will summarize them.", color = colors.textMuted, modifier = Modifier.padding(bottom = 8.dp))
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().size(120.dp),
                        placeholder = { Text("e.g. Met for coffee. Pain point: old software. Budget: $10k. Action: send demo.") }
                    )
                }
            },
            confirmButton = {
                ConnectPrimaryButton(
                    text = "Log & Analyze",
                    onClick = {
                        showDialog = false
                        viewModel.logMeeting(contactId, notes) {
                            notes = ""
                        }
                    }
                )
            },
            dismissButton = {
                ConnectSecondaryButton(text = "Cancel") { showDialog = false }
            }
        )
    }

    Scaffold(
        topBar = { ConnectTopBar(title = "Timeline", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colors.accent
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Meeting", tint = colors.surface1)
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = colors.accent)
            }
        } else if (events.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No timeline events yet", color = colors.textMuted, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(Spacing.md)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(events) { event ->
                    TimelineRow(event, onClick = { if (event.isMeeting) onOpenMeetingSummary(contactId) })
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = event.color.tints(colors)
    Row(
        modifier = Modifier.clickable(enabled = event.isMeeting, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(26.dp).background(bg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(event.icon.imageVector(), contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
        }
        Column(modifier = Modifier.padding(start = Spacing.xs)) {
            Text(event.label, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (event.isMeeting) {
                Text("Tap for AI meeting summary", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun TimelineScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        TimelineScreen(contactId = "preview", onBack = {}, onOpenMeetingSummary = {})
    }
}
