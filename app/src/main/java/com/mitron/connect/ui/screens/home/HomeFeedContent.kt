package com.mitron.connect.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.toImageModel
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.data.model.ChatPreview
import com.mitron.connect.data.model.Event
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedContent(
    onOpenSearch: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    onDraftFollowUp: (String) -> Unit,
    onScheduleMeeting: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    unreadCount: Int,
    chats: List<ChatPreview>,
    events: List<Event>,
    currentUser: com.mitron.connect.data.model.Contact? = null
) {
    var selectedFilter by remember { mutableStateOf("All") }
    
    val displayChats = when (selectedFilter) {
        "Unread" -> chats.filter { it.unreadCount > 0 }
        "Groups" -> chats.filter { it.contactId.isEmpty() }
        else -> chats
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onOpenProfile("my_card") }
            ) {
                if (currentUser?.avatarUrl != null) {
                    AsyncImage(
                        model = currentUser.avatarUrl.toImageModel(),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Avatar(
                        initials = currentUser?.initials ?: "ME",
                        size = AvatarSize.medium,
                        background = Color(0xFFF0F0FA),
                        foreground = Color(0xFF2D31FA)
                    )
                }
            }
            
            Text(
                text = "Connect",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B) // slate-800
            )
            
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    imageVector = Icons.Outlined.Edit, // The write icon
                    contentDescription = "New Message",
                    tint = Color(0xFF475569) // slate-600
                )
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onOpenSearch() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search people, companies, chats...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                enabled = false // Read-only look, clicks should trigger search screen
            )
        }
        
        // Filter Chips
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem("All", selectedFilter == "All") { selectedFilter = "All" }
            val unreadText = if (unreadCount > 0) "Unread ($unreadCount)" else "Unread"
            FilterChipItem(unreadText, selectedFilter == "Unread") { selectedFilter = "Unread" }
            FilterChipItem("Groups", selectedFilter == "Groups") { selectedFilter = "Groups" }
            FilterChipItem("Events", selectedFilter == "Events") { selectedFilter = "Events" }
        }
        
        // Content
        if (selectedFilter == "Events") {
            // Show Events List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (events.isEmpty()) {
                    item {
                        Text("No events right now.", color = Color(0xFF767680), modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(events) { event ->
                        EventItem(event, onClick = { onOpenEvent(event.id) })
                    }
                }
            }
        } else {
            // Show Chats List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (displayChats.isEmpty()) {
                    item {
                        Text(
                            "No chats found.",
                            color = Color(0xFF767680),
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(displayChats) { chat ->
                        ChatItem(chat, onClick = { onOpenChat(chat.id) }, onOpenProfile = onOpenProfile)
                        HorizontalDivider(
                            color = Color(0xFFE4E1E7).copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        
        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF3F4F6),
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val parts = text.split(" (")
            Text(
                text = parts[0],
                color = if (isSelected) Color.White else Color(0xFF4B5563),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (parts.size > 1) {
                val count = parts[1].removeSuffix(")")
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatPreview, onClick: () -> Unit, onOpenProfile: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable { onOpenProfile(chat.contactId) }
        ) {
            Avatar(
                initials = chat.initials,
                size = AvatarSize.large,
                background = Color(0xFF0F172A), // Slate 900 for default fallback
                foreground = Color.White
            )
            // Show online dot only if backend marks the contact as online
            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .background(Color(0xFF22C55E), CircleShape) // Green 500
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = chat.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B), // slate-800
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = chat.formattedTime,
                    fontSize = 11.sp,
                    color = if (chat.unreadCount > 0) Color(0xFF2563EB) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (chat.lastMessage.contains("Voice note", ignoreCase = true)) {
                        Icon(Icons.Outlined.Mic, contentDescription = "Voice note", tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    }
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B), // slate-500
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (chat.contactId.isNotEmpty()) {
                    // Show double checkmark for read messages
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = "Read",
                        tint = Color(0xFF22C55E), // green-500
                        modifier = Modifier.padding(start = 8.dp).size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, onClick: () -> Unit) {
    com.mitron.connect.ui.components.GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.05f)),
        
        
        
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    
                    color = Color(0xFFF0F0FA),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = "Event",
                        tint = Color(0xFF2D31FA),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1B1B1F)
                    )
                    Text(
                        text = event.date ?: "Upcoming",
                        fontSize = 14.sp,
                        color = Color(0xFF2D31FA),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = event.description ?: "Event details.",
                fontSize = 14.sp,
                color = Color(0xFF454557),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (event.location != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF767680),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location!!,
                        fontSize = 12.sp,
                        color = Color(0xFF767680)
                    )
                }
            }
        }
    }
}
