package com.mitron.connect.ui.screens.profile
import androidx.compose.ui.graphics.graphicsLayer

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.RelationshipType
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.tints

val BackgroundColor = Color(0xFFFAF8FF)
val PrimaryColor = Color(0xFF0900DB)
val PrimaryContainer = Color(0xFF2D31FA)
val OnPrimaryContainer = Color(0xFFC8CAFF)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceContainer = Color(0xFFEAEDFF)
val GlassColor = Color.White.copy(alpha = 0.6f)
val GlassBorder = Color.White.copy(alpha = 0.4f)

@Composable
fun ProfileScreen(
    contactId: String,
    onBack: () -> Unit,
    onOpenTimeline: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onOpenBusinessCard: (String) -> Unit,
    onOpenRelationshipScore: (String) -> Unit = {},
    onOpenChat: (String) -> Unit = {}
) {
    val viewModel: ProfileViewModel = viewModel()
    val colors = ConnectTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(contactId) {
        if (contactId == "my_card") viewModel.loadCurrentUser()
        else viewModel.loadContact(contactId)
    }

    val contact by viewModel.contact.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()

    val currentContact = contact ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    val (avatarBg, avatarFg) = currentContact.color.tints(colors)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Top App Bar (Borderless, no title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundColor.copy(alpha = 0.8f))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryColor)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isCurrentUser) {
                IconButton(onClick = onEditProfile, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = PrimaryColor)
                }
            } else {
                IconButton(onClick = { onEditProfile() }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = PrimaryColor)
                }
            }
        }

        val scrollState = rememberScrollState()
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Section with Parallax
            val scrollOffset = scrollState.value.toFloat()
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(128.dp)
                    .graphicsLayer {
                        translationY = scrollOffset * 0.5f // Parallax effect
                        alpha = 1f - (scrollOffset / 500f).coerceIn(0f, 1f) // Fade out
                    },
                contentAlignment = Alignment.Center
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .shadow(12.dp, CircleShape, spotColor = PrimaryColor.copy(alpha = 0.12f))
                        .background(Color.White, CircleShape)
                        .border(4.dp, BackgroundColor, CircleShape)
                ) {
                    if (currentContact.avatarUrl?.isNotBlank() == true && currentContact.avatarUrl != "null") {
                        AsyncImage(
                            model = currentContact.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Avatar(
                            initials = currentContact.initials ?: "",
                            size = 120.dp,
                            background = avatarBg,
                            foreground = avatarFg,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Online Status Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(20.dp)
                        .background(Color(0xFF22C55E), CircleShape) // Green 500
                        .border(2.dp, BackgroundColor, CircleShape)
                )
            }

            Text(
                text = currentContact.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                val badgeColor = when (currentContact.relationshipType) {
                    RelationshipType.PROSPECT -> PrimaryColor
                    RelationshipType.CUSTOMER -> Color(0xFF10B981) // Green
                    RelationshipType.CANDIDATE -> Color(0xFFF59E0B) // Amber
                    RelationshipType.GENERAL -> OnSurfaceVariantColor
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentContact.relationshipType.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentContact.title ?: "Professional",
                    fontSize = 16.sp,
                    color = OnSurfaceVariantColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Pulse Header (Health Score + Topics)
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val healthColor = when {
                        currentContact.score >= 80 -> Color(0xFF10B981)
                        currentContact.score >= 50 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    Text("Relationship Health: ${currentContact.score}/100", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = healthColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SaaS", "Enterprise", "B2B").forEach { tag ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(tag, fontSize = 12.sp, color = OnSurfaceVariantColor)
                            }
                        }
                    }
                }
            }

            // Segmented Control
            var selectedSegment by remember { mutableStateOf(0) }
            val segments = listOf("Timeline", "About", "Insights")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceContainerHigh).padding(4.dp)
            ) {
                segments.forEachIndexed { index, segment ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedSegment == index) Color.White else Color.Transparent)
                            .clickable { selectedSegment = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(segment, fontSize = 14.sp, fontWeight = if (selectedSegment == index) FontWeight.Bold else FontWeight.Medium, color = if (selectedSegment == index) PrimaryColor else OnSurfaceVariantColor)
                    }
                }
            }

            if (selectedSegment == 0) {
                // Show timeline insights or trigger navigation
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Button(onClick = { onOpenTimeline(currentContact.id) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) {
                        Text("View Full Timeline")
                    }
                }
            } else if (selectedSegment == 2) {
                // Insights Segment
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GlassColor)
                        .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Actionable Insights", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor, modifier = Modifier.padding(bottom = 12.dp))
                        
                        Text("Meeting Summaries & Next Steps will appear here.", color = OnSurfaceVariantColor, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                        
                        Button(
                            onClick = {
                                // Invoke DraftHelper
                                com.mitron.connect.services.DraftHelper.draftFollowUpEmail(context, currentContact.email ?: "", currentContact.name, emptyList())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Draft Follow-up")
                        }
                    }
                }
            } else if (selectedSegment == 1) {
                Text(
                    text = currentContact.company ?: "Independent",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = PrimaryColor, modifier = Modifier.size(16.dp))
            }

            // Quick Actions Bento
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(Icons.Outlined.ChatBubbleOutline, "Message", isMuted = false, modifier = Modifier.weight(1f), onClick = { onOpenChat(currentContact.id) })
                QuickActionButton(Icons.Outlined.Call, "Call", isMuted = false, modifier = Modifier.weight(1f), onClick = {
                    scope.launch {
                        try {
                            val userId = com.mitron.connect.data.SessionManager.getUserId()
                            if (userId != null) {
                                val res = com.mitron.connect.data.RetrofitClient.apiService.initiateCall(
                                    com.mitron.connect.data.InitiateCallRequest(callerId = userId, receiverId = currentContact.id, isVideo = false)
                                )
                                if (res.success && res.token != null && res.channelName != null) {
                                    com.mitron.connect.services.CallManager.joinCall(res.token, res.channelName, false)
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                })
                QuickActionButton(Icons.Outlined.Share, "Share", isMuted = false, modifier = Modifier.weight(1f), onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Connect with ${currentContact.name} on Mitron: mitron.app/u/${currentContact.id}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                })
                QuickActionButton(Icons.Outlined.MoreHoriz, "More", isMuted = true, modifier = Modifier.weight(1f), onClick = {
                    android.widget.Toast.makeText(context, "More options coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                })
            }

            // About Section Glass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassColor)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = PrimaryColor.copy(alpha = 0.04f))
                    .padding(20.dp)
            ) {
                Column {
                    Text("About", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor, modifier = Modifier.padding(bottom = 12.dp))
                    
                    if (isCurrentUser) {
                        var aboutText by remember { mutableStateOf(currentContact.about ?: "") }
                        var isEditingAbout by remember { mutableStateOf(false) }

                        if (isEditingAbout) {
                            OutlinedTextField(
                                value = aboutText,
                                onValueChange = { aboutText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Write something about yourself...", color = OnSurfaceVariantColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = OnSurfaceVariantColor)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { 
                                    isEditingAbout = false 
                                }) {
                                    Text("Cancel", color = OnSurfaceVariantColor)
                                }
                                Button(
                                    onClick = { 
                                        isEditingAbout = false 
                                        currentContact.about = aboutText
                                        // TODO: Save to backend
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                                ) {
                                    Text("Save", color = Color.White)
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (aboutText.isNotBlank()) aboutText else "No about info provided.",
                                    fontSize = 16.sp,
                                    color = OnSurfaceVariantColor,
                                    lineHeight = 24.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isEditingAbout = true }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Edit About", tint = PrimaryColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    } else {
                        Text(
                            text = currentContact.about ?: "No about info provided.",
                            fontSize = 16.sp,
                            color = OnSurfaceVariantColor,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // Contact Info Glass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassColor)
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = PrimaryColor.copy(alpha = 0.04f))
                    .padding(20.dp)
            ) {
                Column {
                    ContactDetailItem(Icons.Outlined.LocationOn, "Bangalore, India", false) 
                    HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    
                    if (!currentContact.website.isNullOrEmpty()) {
                        ContactDetailItem(Icons.Outlined.Language, currentContact.website!!, true)
                        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    }
                    if (currentContact.email.isNotEmpty()) {
                        ContactDetailItem(Icons.Outlined.Email, currentContact.email, false)
                        HorizontalDivider(color = SurfaceContainerHigh, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    }
                    if (!currentContact.phone.isNullOrEmpty()) {
                        ContactDetailItem(Icons.Outlined.Call, currentContact.phone!!, false)
                    }
                }
            }

            // Major Action Buttons
            if (!isCurrentUser) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                                data = android.provider.CalendarContract.Events.CONTENT_URI
                                putExtra(android.provider.CalendarContract.Events.TITLE, "Meeting with ${currentContact.name}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = OnPrimaryContainer, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule Meeting", color = OnPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = { 
                            val url = currentContact.website?.let { if (it.startsWith("http")) it else "https://$it" } ?: "https://google.com/search?q=${android.net.Uri.encode(currentContact.company)}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceContainer, contentColor = PrimaryColor),
                        border = BorderStroke(1.dp, Color(0xFFC6C4DA))
                    ) {
                        Icon(Icons.Outlined.Domain, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Company", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = colors.danger),
                    border = BorderStroke(1.dp, colors.danger)
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, isMuted: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHigh)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if(isMuted) Color(0xFFDAE2FD) else PrimaryColor.copy(alpha = 0.1f), CircleShape), // surface-variant or primary/10
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = if(isMuted) OnSurfaceColor else PrimaryColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = OnSurfaceVariantColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ContactDetailItem(icon: ImageVector, value: String, isLink: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SurfaceContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (isLink) PrimaryColor else OnSurfaceColor)
    }
}
