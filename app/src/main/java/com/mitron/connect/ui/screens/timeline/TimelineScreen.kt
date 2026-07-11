package com.mitron.connect.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.mitron.connect.data.model.AccentColor

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
        containerColor = Color(0xFFF9FAFB), // bg-gray-50
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF2563EB), // blue-600
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Meeting", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Go back", tint = Color(0xFF4B5563), modifier = Modifier.size(24.dp)) // gray-600
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Mock Avatar for header
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFE5E7EB), CircleShape), // gray-200
                    contentAlignment = Alignment.Center
                ) {
                    Text("RS", color = Color(0xFF4B5563), fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Rahul Sharma",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827) // gray-900
                )
            }
            
            // Tab Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Timeline", color = Color(0xFF2563EB), fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(2.dp).width(60.dp).background(Color(0xFF2563EB)))
                }
                Text("Notes", color = Color(0xFF9CA3AF), fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                Text("Files", color = Color(0xFF9CA3AF), fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                Text("Tasks", color = Color(0xFF9CA3AF), fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6))) // border-b gray-100

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else if (events.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No timeline events yet", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Vertical Timeline Line
                    Box(
                        modifier = Modifier
                            .padding(start = 69.dp, top = 48.dp)
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFFE5E7EB)) // gray-200
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                    ) {
                        item {
                            val calendar = java.util.Calendar.getInstance()
                            calendar.time = events.firstOrNull()?.createdAt ?: java.util.Date()
                            val year = calendar.get(java.util.Calendar.YEAR)
                            Text(
                                text = year.toString(),
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color(0xFF9CA3AF),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                        
                        items(events) { event ->
                            TimelineRow(event, onClick = { if (event.isMeeting) onOpenMeetingSummary(contactId) })
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent, onClick: () -> Unit) {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = event.createdAt
    val month = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(calendar.time).uppercase()
    val date = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(calendar.time)

    val colorHex = when (event.color) {
        AccentColor.SUCCESS -> 0xFF22C55E // green-500
        AccentColor.ACCENT -> 0xFF2563EB // blue-600
        else -> 0xFFD1D5DB // gray-300
    }
    
    val isPast = event.color == AccentColor.NEUTRAL

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .clickable(enabled = event.isMeeting, onClick = onClick),
        verticalAlignment = Alignment.Top,
    ) {
        // Date Column
        Column(
            modifier = Modifier.width(40.dp).let { if (isPast) it.alpha(0.5f) else it },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(month, fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(colorHex))
            Text(date, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF111827)) // gray-900
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Dot & Content
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // Dot
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(12.dp)
                    .background(Color(colorHex), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Content
            Column {
                Text(event.label, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF111827)) // gray-900
                if (event.subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(event.subtitle!!, fontSize = 12.sp, color = Color(0xFF6B7280)) // gray-500
                }
                
                if (event.isMeeting) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFAF5FF), CircleShape) // purple-50
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(12.dp)) // purple-700
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Summary available", fontSize = 10.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = Color(0xFF7E22CE))
                    }
                }
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
