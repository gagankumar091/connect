package com.mitron.connect.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material.icons.outlined.*
import com.mitron.connect.data.model.Contact
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.components.toImageModel
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.components.QrCode
import com.mitron.connect.ui.components.Tag
import com.mitron.connect.ui.components.TagStyle
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.ui.platform.LocalContext

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

    LaunchedEffect(contactId) {
        if (contactId == "my_card") viewModel.loadCurrentUser()
        else viewModel.loadContact(contactId)
    }

    val contact by viewModel.contact.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()

    val currentContact = contact ?: run {
        if (contactId == "preview") {
            Contact(
                id = "preview", name = "Jane Doe", title = "Senior Developer",
                company = "Mitron Tech", email = "jane.doe@example.com",
                phone = "+1 987 654 3210", linkedin = "linkedin.com/in/janedoe",
                website = "janedoe.dev", initials = "JD", score = 92,
                daysSinceContact = 3, mutualsCount = 15,
                nextAction = "Send follow-up email about the new project.",
                sharedInterests = listOf("Kotlin", "Compose", "Design")
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.fillPrimary,
                    strokeWidth = 3.dp
                )
            }
            return
        }
    }

    val (avatarBg, avatarFg) = currentContact.color.tints(colors)

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint = Color(0xFF1E293B)
                    )
                }
                if (contactId == "my_card") {
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit profile",
                            tint = Color(0xFF4B5563) // gray-600
                        )
                    }
                } else {
                    IconButton(onClick = { onOpenBusinessCard(currentContact.id) }) {
                        Icon(
                            Icons.Filled.IosShare,
                            contentDescription = "Share profile",
                            tint = Color(0xFF4B5563)
                        )
                    }
                }
            }
            
            // Profile Identity
            Box(modifier = Modifier.padding(top = 16.dp)) {
                if (currentContact.avatarUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = currentContact.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.border(4.dp, Color.White, CircleShape).clip(CircleShape)) {
                        Avatar(
                            initials = currentContact.initials ?: "",
                            size = AvatarSize.large, // Wait, AvatarSize.large might not be 96dp. I will wrap it.
                            background = avatarBg,
                            foreground = avatarFg,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }
                // Online dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp, end = 4.dp)
                        .size(20.dp)
                        .background(Color(0xFF22C55E), CircleShape) // green-500
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    currentContact.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827) // gray-900
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF3B82F6), // blue-500
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Title & Company
            if (!currentContact.title.isNullOrEmpty() || !currentContact.company.isNullOrEmpty()) {
                if (!currentContact.title.isNullOrEmpty()) {
                    Text(
                        currentContact.title!!,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280) // gray-500
                    )
                }
                if (!currentContact.company.isNullOrEmpty()) {
                    Text(
                        currentContact.company!!,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB) // blue-600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val scope = rememberCoroutineScope()
                if (isCurrentUser) {
                    QuickActionButton(Icons.Filled.Edit, "Edit Profile", onClick = onEditProfile)
                } else {
                    QuickActionButton(Icons.Outlined.ChatBubbleOutline, "Message", onClick = { onOpenChat(currentContact.id) })
                    QuickActionButton(Icons.Outlined.Call, "Audio Call", onClick = {
                        scope.launch {
                            try {
                                val userId = com.mitron.connect.data.SessionManager.getUserId()
                                if (userId == null) {
                                    android.widget.Toast.makeText(context, "User ID not found", android.widget.Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val res = com.mitron.connect.data.RetrofitClient.apiService.initiateCall(
                                    com.mitron.connect.data.InitiateCallRequest(
                                        callerId = userId,
                                        receiverId = currentContact.id,
                                        isVideo = false
                                    )
                                )
                                if (res.success && res.token != null && res.channelName != null) {
                                    com.mitron.connect.services.CallManager.joinCall(res.token, res.channelName, false)
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to start call", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Network error starting call", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    QuickActionButton(Icons.Filled.Videocam, "Video Call", onClick = {
                        scope.launch {
                            try {
                                val userId = com.mitron.connect.data.SessionManager.getUserId()
                                if (userId == null) {
                                    android.widget.Toast.makeText(context, "User ID not found", android.widget.Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                val res = com.mitron.connect.data.RetrofitClient.apiService.initiateCall(
                                    com.mitron.connect.data.InitiateCallRequest(
                                        callerId = userId,
                                        receiverId = currentContact.id,
                                        isVideo = true
                                    )
                                )
                                if (res.success && res.token != null && res.channelName != null) {
                                    com.mitron.connect.services.CallManager.joinCall(res.token, res.channelName, true)
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to start video call", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Network error starting video call", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    QuickActionButton(Icons.Outlined.AutoAwesome, "AI Score", onClick = { onOpenRelationshipScore(currentContact.id) })
                }
                QuickActionButton(Icons.Outlined.Share, "Share", onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Connect with ${currentContact.name}")
                        putExtra(Intent.EXTRA_TEXT, "Connect with ${currentContact.name} on Mitron Connect App! Profile: mitron.app/u/${currentContact.username ?: currentContact.id}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                })
                QuickActionButton(Icons.Outlined.MoreHoriz, "More", onClick = {
                    android.widget.Toast.makeText(context, "More options coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // QR Code Section
            if (contactId == "my_card") {
                var flipped by remember { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (flipped) 180f else 0f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
                    label = "cubeRotate"
                )
                
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable {
                            flipped = !flipped
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Connect with me on Mitron: mitron.app/u/${currentContact.id}")
                            }
                            // Only share if long pressed in real app, tap flips cube
                        },
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                ) {
                    if (rotation <= 90f) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("My Mitron Code", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QrCode2, contentDescription = "QR", modifier = Modifier.size(64.dp), tint = Color.Black)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Tap to Flip", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    } else {
                        // Back side of the cube
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .graphicsLayer { rotationY = 180f }, // un-mirror text
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Bio & Status", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Building the future of communication. Always open to collaborate!",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Connect with me on Mitron: mitron.app/u/${currentContact.id}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Mitron Code"))
                            }) {
                                Text("Share Profile")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // About & Contact Details
            val hasDetails = !currentContact.website.isNullOrEmpty() || currentContact.email.isNotEmpty() || !currentContact.phone.isNullOrEmpty()
            if (hasDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Contact Info",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827) // gray-900
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Contact Details List
                    if (!currentContact.website.isNullOrEmpty()) {
                        ContactDetailItem(Icons.Outlined.Language, currentContact.website!!, isLink = true)
                    }
                    if (currentContact.email.isNotEmpty()) {
                        ContactDetailItem(Icons.Outlined.Email, currentContact.email, isLink = false)
                    }
                    if (!currentContact.phone.isNullOrEmpty()) {
                        ContactDetailItem(Icons.Outlined.Phone, currentContact.phone!!, isLink = false)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
            
            // Bottom Action Cards (Schedule & Company)
            if (contactId != "my_card") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                                data = android.provider.CalendarContract.Events.CONTENT_URI
                                putExtra(android.provider.CalendarContract.Events.TITLE, "Meeting with ${currentContact.name}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)), // indigo-50
                        border = BorderStroke(1.dp, Color(0xFFE0E7FF)), // indigo-100
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.Event, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp)) // indigo-600
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule\nMeeting", color = Color(0xFF4F46E5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    Button(
                        onClick = { 
                            val url = currentContact.website?.let { 
                                if (it.startsWith("http")) it else "https://$it"
                            } ?: "https://google.com/search?q=${android.net.Uri.encode(currentContact.company)}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEF2FF)), // indigo-50
                        border = BorderStroke(1.dp, Color(0xFFE0E7FF)), // indigo-100
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.Business, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Company", color = Color(0xFF4F46E5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Bottom Buttons
            if (contactId == "my_card") {
                var isLoggingOut by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = {
                        isLoggingOut = true
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, colors.danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger)
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log out", fontWeight = FontWeight.SemiBold)
                }
                
                if (isLoggingOut) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.8f)).clickable(enabled=false){}, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.fillPrimary)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEEF2FF), // indigo-50
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp)) // indigo-600
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4B5563), // gray-600
            letterSpacing = (-0.5).sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ContactDetailItem(icon: ImageVector, text: String, isLink: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)), // gray-100
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF6B7280), // gray-500
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            fontSize = 14.sp,
            color = if (isLink) Color(0xFF2563EB) else Color(0xFF374151), // blue-600 or gray-700
            fontWeight = if (isLink) FontWeight.Normal else FontWeight.Normal
        )
    }
}
