package com.mitron.connect.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.mitron.connect.data.model.AppNotification
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.screens.home.HomeViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    
    val upcoming = notifications.filter { it.isPastDue != true }
    val pastDue = notifications.filter { it.isPastDue == true }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Follow up Reminders", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text("See all", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2563EB), modifier = Modifier.clickable { })
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF3F4F6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavIcon(Icons.Outlined.ChatBubbleOutline, "Chats", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.Call, "Calls", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.People, "Contacts", Color(0xFF9CA3AF))
                    BottomNavIcon(Icons.Outlined.Event, "Events", Color(0xFF9CA3AF))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }) {
                        Box {
                            Icon(Icons.Outlined.RemoveRedEye, contentDescription = "AI", tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                            Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(8.dp).background(Color(0xFFEF4444), CircleShape).border(2.dp, Color.White, CircleShape))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("AI", fontSize = 10.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        text = "UPCOMING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(upcoming) { notif ->
                    ReminderRow(notif, onOpenProfile)
                }
            }

            if (pastDue.isNotEmpty()) {
                item {
                    Text(
                        text = "PAST DUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444), // red-500
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                }
                items(pastDue) { notif ->
                    ReminderRow(notif, onOpenProfile)
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(notification: AppNotification, onClick: (String) -> Unit) {
    val isPast = notification.isPastDue == true
    val textColor = if (isPast) Color(0xFFEF4444) else Color(0xFF6B7280)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clickable { notification.action_id?.let { onClick(it) } },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!notification.avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = notification.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
        } else {
            Avatar(initials = notification.title.take(2).uppercase(), size = com.mitron.connect.ui.theme.AvatarSize.medium, modifier = Modifier.size(48.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.dueDate ?: notification.description ?: "", 
                fontSize = 12.sp, 
                fontWeight = if (isPast) FontWeight.Medium else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun BottomNavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {  }) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
