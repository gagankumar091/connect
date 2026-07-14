package com.mitron.connect.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.TimelineEvent
import com.mitron.connect.data.model.TimelineIcon
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectSecondaryButton
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone

val BackgroundColor = Color(0xFFFAF8FF)
val SurfaceColor = Color(0xFFFAF8FF)
val SurfaceContainerLow = Color(0xFFF2F3FF)
val SurfaceVariant = Color(0xFFDAE2FD)
val PrimaryColor = Color(0xFF0900DB)
val PrimaryContainer = Color(0xFF2D31FA)
val SecondaryContainer = Color(0xFF00CCF9)
val OutlineVariant = Color(0xFFC6C4DA)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val SuccessColor = Color(0xFF137333)
val SuccessContainer = Color(0xFFE6F4EA)

private fun TimelineIcon.imageVector(): ImageVector = when (this) {
    TimelineIcon.LOCATION -> Icons.Filled.LocationOn
    TimelineIcon.QR -> Icons.Filled.QrCode
    TimelineIcon.FILE -> Icons.Filled.Description
    TimelineIcon.COFFEE -> Icons.Filled.ChatBubble
    TimelineIcon.SEND -> Icons.Filled.Event
    TimelineIcon.CHECK -> Icons.Filled.TaskAlt
    TimelineIcon.EMAIL -> Icons.Filled.Email
    TimelineIcon.CALENDAR -> Icons.Filled.CalendarToday
    TimelineIcon.NOTE -> Icons.Filled.Note
}

@Composable
fun TimelineScreen(
    contactId: String,
    onBack: () -> Unit,
    onOpenMeetingSummary: (String) -> Unit,
    viewModel: TimelineViewModel = viewModel()
) {
    LaunchedEffect(contactId) {
        viewModel.loadTimeline(contactId)
    }
    
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedOutcome by remember { mutableStateOf("Positive") }
    val outcomes = listOf("Positive", "Neutral", "Follow-up")
    
    var selectedTab by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current // 0: Timeline, 1: Notes, 2: Files, 3: Tasks

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log a Meeting", color = OnSurfaceColor) },
            text = {
                Column {
                    Text("Select Outcome", color = OnSurfaceColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        outcomes.forEach { outcome ->
                            val isSelected = selectedOutcome == outcome
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) PrimaryColor else SurfaceVariant)
                                    .clickable { selectedOutcome = outcome }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(outcome, color = if (isSelected) Color.White else OnSurfaceVariantColor, fontSize = 12.sp)
                            }
                        }
                    }
                    Text("Meeting Notes", color = OnSurfaceVariantColor, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("e.g. Met for coffee. Discussed software automation...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val combinedNotes = "Outcome: $selectedOutcome. Notes: $notes"
                        viewModel.logMeeting(contactId, combinedNotes) {
                            showDialog = false
                            notes = ""
                            context.startService(android.content.Intent(context, com.mitron.connect.services.MitronSyncService::class.java))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Log & Analyze", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel", color = PrimaryColor) }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = PrimaryColor.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Meeting", modifier = Modifier.size(24.dp))
            }
        },
        topBar = {
            Column(modifier = Modifier.background(SurfaceColor.copy(alpha = 0.8f))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-8).dp)) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Go back", tint = OnSurfaceColor)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        AsyncImage(
                            model = "https://api.dicebear.com/7.x/initials/svg?seed=Rahul",
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant.copy(alpha = 0.2f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Rahul Sharma",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = OnSurfaceColor
                        )
                    }
                    IconButton(onClick = { /* more options */ }, modifier = Modifier.offset(x = 8.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = OnSurfaceColor)
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryColor,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        androidx.compose.material3.TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 2.dp,
                            color = PrimaryColor
                        )
                    },
                    divider = { HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f)) }
                ) {
                    val tabs = listOf("Timeline", "Notes", "Files", "Tasks")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontSize = 16.sp, 
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTab == index) PrimaryColor else OnSurfaceVariantColor
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (events.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No timeline events yet", color = OnSurfaceVariantColor, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .drawBehind {
                        drawLine(
                            color = SurfaceVariant,
                            start = Offset(x = 42.dp.toPx(), y = 0f),
                            end = Offset(x = 42.dp.toPx(), y = size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
            ) {
                item {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = events.firstOrNull()?.createdAt ?: java.util.Date()
                    val year = calendar.get(java.util.Calendar.YEAR)
                    Text(
                        text = year.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                
                items(events, key = { it.id }) { event ->
                    androidx.compose.animation.AnimatedVisibility(visible = true) {
                        TimelineRow(event, onClick = { if (event.isMeeting) onOpenMeetingSummary(contactId) })
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent, onClick: () -> Unit) {
    val calendar = java.util.Calendar.getInstance()
    var dateString = "01"
    var monthString = "JAN"
    try {
        val date = event.createdAt
        if (date != null) {
            calendar.time = date
            monthString = SimpleDateFormat("MMM", Locale.US).format(date).uppercase()
            dateString = SimpleDateFormat("dd", Locale.US).format(date)
        }
    } catch(e: Exception) { }

    val iconBgColor: Color
    val iconColor: Color
    when (event.icon) {
        TimelineIcon.LOCATION -> {
            iconBgColor = PrimaryContainer
            iconColor = PrimaryColor
        }
        TimelineIcon.QR, TimelineIcon.CHECK -> {
            iconBgColor = SuccessContainer
            iconColor = SuccessColor
        }
        TimelineIcon.COFFEE -> {
            iconBgColor = PrimaryContainer.copy(alpha = 0.2f) // fallback if 50% doesn't look right, wait HTML uses /50
            iconColor = PrimaryColor
        }
        else -> {
            iconBgColor = SurfaceVariant
            iconColor = OnSurfaceColor
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .clickable(enabled = event.isMeeting, onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot column (width 36.dp to align with x=18.dp line, but wait, our padding is 24.dp. 
        // 24.dp + 18.dp = 42.dp. 
        // We drew line at 42.dp in the parent. The row is padded by 24.dp, so inside row, line is at 18.dp.
        // Box width = 36.dp -> center is 18.dp. Matches perfectly!
        Box(modifier = Modifier.width(36.dp).padding(top = 4.dp), contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon.imageVector(),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Date Box
        Column(
            modifier = Modifier.width(50.dp).padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(monthString, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariantColor)
            Text(dateString, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content Card
        com.mitron.connect.ui.components.GlassCard(
            modifier = Modifier.weight(1f)
        ) {
            // Gradient strip for meetings/coffee
            if (event.isMeeting || event.icon == TimelineIcon.COFFEE || event.icon == TimelineIcon.CALENDAR) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(4.dp).background(Brush.horizontalGradient(listOf(SecondaryContainer, PrimaryColor)))
                )
            }
            
            Column(modifier = Modifier.padding(top = if (event.isMeeting || event.icon == TimelineIcon.COFFEE || event.icon == TimelineIcon.CALENDAR) 12.dp else 0.dp)) {
                Text(event.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor)
                if (event.subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(event.subtitle!!, fontSize = 14.sp, color = OnSurfaceVariantColor)
                }
                
                if (event.isMeeting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Summary available", fontSize = 14.sp, color = PrimaryColor)
                    }
                }
            }
        }
    }
}
