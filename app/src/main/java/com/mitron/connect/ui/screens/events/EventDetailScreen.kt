package com.mitron.connect.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.theme.AvatarSize

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit = {},
    viewModel: EventDetailViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }
    
    val event by viewModel.event.collectAsState()
    val attendees by viewModel.attendees.collectAsState()

    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2563EB))
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        IconButton(onClick = onBack, modifier = Modifier.size(24.dp).padding(top = 4.dp, end = 8.dp)) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(event!!.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text(event!!.location ?: "Location Unknown", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                    Text("Change Event", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB), modifier = Modifier.clickable { onBack() })
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nearby (${attendees.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(bottom = 12.dp))
                        Box(modifier = Modifier.height(2.dp).width(80.dp).background(Color(0xFF2563EB)))
                    }
                    Text("People", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 12.dp))
                    Text("Groups", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280), modifier = Modifier.padding(bottom = 12.dp))
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6)))
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    EventBottomNavIcon(Icons.Filled.ChatBubbleOutline, "Chats", Color(0xFF9CA3AF))
                    EventBottomNavIcon(Icons.Outlined.Call, "Calls", Color(0xFF9CA3AF))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-16).dp)
                                .size(48.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = "Discover", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Text("Discover", fontSize = 10.sp, color = Color(0xFF2563EB), modifier = Modifier.offset(y = (-8).dp))
                    }
                    
                    EventBottomNavIcon(Icons.Outlined.Event, "Events", Color(0xFF9CA3AF))
                    EventBottomNavIcon(Icons.Outlined.AutoAwesome, "AI", Color(0xFF9CA3AF))
                }
            }
        }
    ) { innerPadding ->
        if (attendees.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No attendees yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Be the first to join this event!", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(attendees) { contact ->
                    EventAttendeeRow(
                        contact = contact,
                        onClick = { onOpenProfile(contact.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventBottomNavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color)
    }
}

@Composable
private fun EventAttendeeRow(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!contact.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = contact.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                )
            } else {
                Avatar(initials = contact.initials, size = AvatarSize.small)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(contact.title ?: "Mitron User", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color(0xFFDCFCE7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
        }
    }
}
