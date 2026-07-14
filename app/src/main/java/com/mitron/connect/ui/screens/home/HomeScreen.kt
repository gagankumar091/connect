package com.mitron.connect.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.ui.components.ConnectButton
import com.mitron.connect.ui.components.ButtonVariant

// Premium Design Tokens
private val BgColor = Color(0xFFF6F2FA)
private val CardColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFF4343D5)
private val PrimaryContainer = Color(0xFF5D5FEF)
private val SurfaceHigh = Color(0xFFEEEAF4)
private val OnSurface = Color(0xFF1B1B20)
private val OnSurfaceVariant = Color(0xFF464555)
private val OutlineColor = Color(0xFF767586)
private val SuccessColor = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProfile: (String) -> Unit,
    onOpenCompany: (String) -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenSmartSearch: () -> Unit,
    onOpenBusinessCard: (String) -> Unit,
    onOpenHealth: () -> Unit,
    onOpenNotifications: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToChatTab: () -> Unit = {},
    onNavigateToEventsTab: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    val chats by viewModel.chats.collectAsState()
    val events by viewModel.events.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unreadCount = chats.sumOf { it.unreadCount }
    val upcomingEventCount = events.size
    val reminderCount = notifications.size

    val healthScore = if (chats.isNotEmpty()) {
        val activeChats = chats.count { it.unreadCount > 0 || it.lastMessage.isNotEmpty() }
        ((activeChats.toFloat() / chats.size) * 100).toInt().coerceIn(0, 100)
    } else 75

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Surface(
                color = BgColor.copy(alpha = 0.9f),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good morning,", fontSize = 14.sp, color = OutlineColor, fontWeight = FontWeight.Medium)
                        Text(
                            text = currentUser?.name?.split(" ")?.firstOrNull() ?: "Connect",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceHigh)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onOpenNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = PrimaryColor, modifier = Modifier.size(24.dp))
                            if (reminderCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp)
                                        .size(10.dp)
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .border(2.dp, PrimaryContainer, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { currentUser?.id?.let { onOpenProfile(it) } }
                        ) {
                            if (!currentUser?.avatarUrl.isNullOrEmpty() && currentUser?.avatarUrl != "null") {
                                AsyncImage(
                                    model = currentUser!!.avatarUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(listOf(PrimaryColor, PrimaryContainer))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.name?.take(1)?.uppercase() ?: "M",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                DailyBriefingCard(
                    unreadCount = unreadCount,
                    upcomingEventCount = upcomingEventCount,
                    isLoading = isLoading,
                    onViewChats = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToChatTab()
                    },
                    onViewEvents = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToEventsTab()
                    }
                )
            }

            item {
                HealthScoreCard(score = healthScore, onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onOpenHealth() })
            }

            item {
                QuickActionsCard(
                    onCallClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    onMessageClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onNavigateToChatTab() },
                    onSearchClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onOpenSmartSearch() }
                )
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                GrowNetworkCard(onNavigateToContacts = onNavigateToContacts)
            }
        }
    }
}

@Composable
private fun DailyBriefingCard(
    unreadCount: Int,
    upcomingEventCount: Int,
    isLoading: Boolean,
    onViewChats: () -> Unit,
    onViewEvents: () -> Unit
) {
    com.mitron.connect.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        
        
        
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Daily Briefing", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                if (isLoading) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryColor)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            BriefingRow(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = "Unread Messages",
                count = unreadCount,
                actionLabel = "View All",
                onClick = onViewChats
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SurfaceHigh.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            BriefingRow(
                icon = Icons.Outlined.Event,
                label = "Upcoming Events",
                count = upcomingEventCount,
                actionLabel = "View All",
                onClick = onViewEvents
            )
        }
    }
}

@Composable
private fun BriefingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = OutlineColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontSize = 15.sp, color = OnSurface, fontWeight = FontWeight.Medium)
            if (count > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(PrimaryColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(count.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = actionLabel,
            color = PrimaryColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        )
    }
}

@Composable
private fun HealthScoreCard(score: Int, onClick: () -> Unit) {
    com.mitron.connect.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).shadow(8.dp, RoundedCornerShape(24.dp)).clickable { onClick() },
        
        
        
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Network Health", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(16.dp))
            val progressAnim = remember { Animatable(0f) }
            LaunchedEffect(score) {
                progressAnim.animateTo(
                    targetValue = score / 100f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 80f)
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(color = SurfaceHigh, startAngle = 135f, sweepAngle = 270f, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(
                        brush = Brush.linearGradient(listOf(PrimaryColor, PrimaryContainer)),
                        startAngle = 135f,
                        sweepAngle = 270f * progressAnim.value,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text("${(progressAnim.value * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = OnSurface)
            }
        }
    }
}

@Composable
private fun QuickActionsCard(onCallClick: () -> Unit, onMessageClick: () -> Unit, onSearchClick: () -> Unit) {
    com.mitron.connect.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).shadow(8.dp, RoundedCornerShape(24.dp)),
        
        
        
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickActionButton(Icons.Outlined.Call, "Call", onCallClick)
                QuickActionButton(Icons.Outlined.ChatBubbleOutline, "Message", onMessageClick)
                QuickActionButton(Icons.Outlined.Search, "Search", onSearchClick)
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryColor.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun GrowNetworkCard(onNavigateToContacts: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "scale"
    )

    com.mitron.connect.ui.components.GlassCard(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
        
        
        
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
                    .background(Brush.radialGradient(listOf(SuccessColor.copy(alpha = 0.2f), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(SuccessColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Handshake, contentDescription = null, modifier = Modifier.size(28.dp), tint = SuccessColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Ready to grow your network?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Spacer(Modifier.height(4.dp))
            Text("Discover professionals matching your interests", fontSize = 13.sp, color = OutlineColor)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onNavigateToContacts,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Browse Contacts", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}
