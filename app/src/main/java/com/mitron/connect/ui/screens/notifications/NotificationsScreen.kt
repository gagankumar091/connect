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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

val BackgroundColor = Color(0xFFFAF8FF)
val SurfaceColor = Color(0xFFFAF8FF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceVariantColor = Color(0xFFDAE2FD)
val PrimaryColor = Color(0xFF0900DB)
val OnPrimaryColor = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF2D31FA)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val ErrorColor = Color(0xFFBA1A1A)

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
        containerColor = BackgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor.copy(alpha = 0.8f))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).offset(x = (-8).dp)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceColor)
                    }
                    Text("Reminders", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                }
                
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = OnSurfaceColor)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = PrimaryColor,
                contentColor = OnPrimaryColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 80.dp) // Lift above global nav bar
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {} // Suppressed; handled globally
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            if (upcoming.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurfaceColor
                        )
                        Text(
                            text = "See all",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor,
                            modifier = Modifier.clickable { }
                        )
                    }
                }
                items(upcoming) { notif ->
                    ReminderRow(notif, onOpenProfile)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (pastDue.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Past Due",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorColor
                        )
                    }
                }
                items(pastDue) { notif ->
                    ReminderRow(notif, onOpenProfile)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(notification: AppNotification, onClick: (String) -> Unit) {
    val isPast = notification.isPastDue == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = PrimaryContainer.copy(alpha = 0.05f))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { notification.action_id?.let { onClick(it) } }
    ) {
        if (isPast) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .matchParentSize()
                    .background(ErrorColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (!notification.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = notification.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            } else {
                Avatar(
                    initials = notification.title.take(2).uppercase(),
                    size = com.mitron.connect.ui.theme.AvatarSize.medium,
                    modifier = Modifier.size(48.dp).border(2.dp, Color.White, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPast) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = ErrorColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = notification.dueDate ?: notification.description ?: "",
                        fontSize = 14.sp,
                        color = if (isPast) ErrorColor else OnSurfaceVariantColor,
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Check button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceContainerHigh, CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Complete", tint = PrimaryColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}
