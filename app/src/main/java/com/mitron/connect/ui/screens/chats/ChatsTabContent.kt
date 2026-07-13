package com.mitron.connect.ui.screens.chats

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mitron.connect.data.model.ChatPreview
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.tints

// ── Design Tokens ──────────────────────────────────────────────────────────
private val ChatBgColor            = Color(0xFFF6F2FA)
private val ChatSurfaceColor       = Color(0xFFFBF8FF)
private val ChatPrimaryColor       = Color(0xFF4343D5)
internal val ChatPrimaryContainer  = Color(0xFF5D5FEF)
private val ChatSecondaryColor     = Color(0xFF4848D2)
private val ChatErrorColor         = Color(0xFFBA1A1A)
private val ChatOutlineColor       = Color(0xFF767586)
private val ChatOutlineVariantColor= Color(0xFFC7C4D7)
private val ChatOnSurfaceColor     = Color(0xFF1B1B20)
private val ChatOnSurfaceVariant   = Color(0xFF464555)
private val ChatSurfaceContainerLow= Color(0xFFEEEAF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTabContent(
    chats: List<ChatPreview>,
    currentUser: com.mitron.connect.data.model.Contact?,
    onOpenChat: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenScanner: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: (String) -> Unit = {},
    notificationsCount: Int = 0
) {
    val unreadCount = chats.sumOf { it.unreadCount }
    var selectedFilter by remember { mutableStateOf(0) }

    val displayedChats = remember(chats, selectedFilter) {
        when (selectedFilter) {
            1 -> chats.filter { it.unreadCount > 0 }
            2 -> chats.filter { it.contactId.isEmpty() }
            else -> chats
        }
    }

    Scaffold(
        containerColor = ChatBgColor,
        topBar = {
            Surface(
                color = ChatSurfaceColor,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(bottom = 4.dp)
                ) {
                    // ── App Bar ──────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { currentUser?.let { onOpenProfile(it.id) } }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .border(2.dp, ChatPrimaryContainer, CircleShape)
                            ) {
                                if (!currentUser?.avatarUrl.isNullOrEmpty() && currentUser?.avatarUrl != "null") {
                                    AsyncImage(
                                        model = currentUser!!.avatarUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(ChatPrimaryColor, ChatPrimaryContainer)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser?.name?.take(1)?.uppercase() ?: "M",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                            Column {
                                Text(
                                    text = "Connect",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChatOnSurfaceColor
                                )
                                if (!currentUser?.name.isNullOrEmpty()) {
                                    Text(
                                        text = currentUser!!.name,
                                        fontSize = 12.sp,
                                        color = ChatOutlineColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(
                                onClick = onOpenScanner,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ChatSurfaceContainerLow)
                            ) {
                                Icon(
                                    Icons.Outlined.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = ChatPrimaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(
                                    onClick = onOpenNotifications,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ChatPrimaryColor)
                                ) {
                                    Icon(
                                        Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (notificationsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-2).dp, y = 2.dp)
                                            .size(10.dp)
                                            .background(ChatErrorColor, CircleShape)
                                            .border(1.dp, ChatSurfaceColor, CircleShape)
                                    ) {}
                                }
                            }
                        }
                    }

                    // ── Search Bar ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(ChatSurfaceContainerLow)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenSearch
                            )
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = ChatOutlineColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search people, chats...",
                                fontSize = 15.sp,
                                color = ChatOutlineColor
                            )
                        }
                    }

                    // ── Filter Chips ──────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ChipItem("All", isSelected = selectedFilter == 0) { selectedFilter = 0 }
                        ChipItem(
                            label = if (unreadCount > 0) "Unread ($unreadCount)" else "Unread",
                            isSelected = selectedFilter == 1,
                            badgeCount = unreadCount
                        ) { selectedFilter = 1 }
                        ChipItem("Groups", isSelected = selectedFilter == 2) { selectedFilter = 2 }
                        ChipItem("Events", isSelected = false) { onOpenEvents() }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = ChatOutlineVariantColor.copy(alpha = 0.3f))
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 110.dp)
        ) {
            if (displayedChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = ChatOutlineVariantColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No conversations yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatOnSurfaceColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start connecting with people",
                                fontSize = 14.sp,
                                color = ChatOutlineColor
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(displayedChats) { index, chat ->
                    ChatRowItem(chat = chat, onClick = { onOpenChat(chat.id) })
                    if (index < displayedChats.lastIndex) {
                        HorizontalDivider(
                            color = ChatOutlineVariantColor.copy(alpha = 0.3f),
                            modifier = Modifier.padding(start = 84.dp, end = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipItem(
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(if (isSelected) ChatPrimaryColor else ChatSurfaceContainerLow)
    val contentColor by animateColorAsState(if (isSelected) Color.White else ChatOnSurfaceColor)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ChatRowItem(chat: ChatPreview, onClick: () -> Unit) {
    val (bg, fg) = chat.color.tints(ConnectTheme.colors)
    val isUnread = chat.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isUnread) ChatPrimaryColor.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.initials.take(2).uppercase(),
                color = fg,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            // Online Badge Example - Placeholder for now
            if (false) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .background(Color(0xFF22C55E), CircleShape)
                        .border(2.dp, ChatSurfaceColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontSize = 16.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = ChatOnSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chat.formattedTime,
                    fontSize = 12.sp,
                    color = if (isUnread) ChatPrimaryColor else ChatOutlineColor,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // If the last message was a voice note
                if (chat.lastMessage.contains("Voice message") || chat.lastMessage.contains("Audio")) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Audio",
                        tint = if (isUnread) ChatOnSurfaceColor else ChatOutlineColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = if (isUnread) ChatOnSurfaceColor else ChatOutlineColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(ChatPrimaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
