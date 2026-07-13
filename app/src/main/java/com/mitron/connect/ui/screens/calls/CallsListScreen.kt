package com.mitron.connect.ui.screens.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.InitiateCallRequest
import com.mitron.connect.data.RetrofitClient
import com.mitron.connect.data.SessionManager
import com.mitron.connect.data.model.Contact
import com.mitron.connect.services.CallManager
import com.mitron.connect.ui.screens.home.HomeViewModel
import kotlinx.coroutines.launch

private val CallsBg = Color(0xFFFAF8FF)
private val CallsPrimary = Color(0xFF4343D5)
private val CallsOnSurface = Color(0xFF1B1B20)
private val CallsVariant = Color(0xFF454557)
private val CallsSurface = Color(0xFFE4E1E9)

@Composable
fun CallsListScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CallsBg)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calls",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CallsOnSurface
            )
            Text(
                text = "${contacts.size} contacts",
                fontSize = 13.sp,
                color = CallsVariant
            )
        }

        HorizontalDivider(color = CallsSurface, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading && contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CallsPrimary)
            }
        } else if (contacts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = CallsSurface, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No contacts yet", fontSize = 16.sp, color = CallsVariant)
                    Text("Connect with people to call them", fontSize = 13.sp, color = CallsVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(contacts) { contact ->
                    CallContactRow(
                        contact = contact,
                        onAudioCall = {
                            scope.launch {
                                try {
                                    val myId = SessionManager.getUserId() ?: return@launch
                                    val res = RetrofitClient.apiService.initiateCall(
                                        InitiateCallRequest(myId, contact.id, false)
                                    )
                                    if (res.success && res.token != null && res.channelName != null) {
                                        CallManager.joinCall(res.token, res.channelName, false)
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        onVideoCall = {
                            scope.launch {
                                try {
                                    val myId = SessionManager.getUserId() ?: return@launch
                                    val res = RetrofitClient.apiService.initiateCall(
                                        InitiateCallRequest(myId, contact.id, true)
                                    )
                                    if (res.success && res.token != null && res.channelName != null) {
                                        CallManager.joinCall(res.token, res.channelName, true)
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    )
                    HorizontalDivider(
                        color = CallsSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 80.dp, end = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallContactRow(
    contact: Contact,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(52.dp)) {
            if (!contact.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = contact.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CallsSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials.take(2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CallsPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name + title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CallsOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!contact.title.isNullOrEmpty() || !contact.company.isNullOrEmpty()) {
                Text(
                    text = buildString {
                        if (!contact.title.isNullOrEmpty()) append(contact.title)
                        if (!contact.title.isNullOrEmpty() && !contact.company.isNullOrEmpty()) append(" · ")
                        if (!contact.company.isNullOrEmpty()) append(contact.company)
                    },
                    fontSize = 12.sp,
                    color = CallsVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Call buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CallsPrimary.copy(alpha = 0.1f), CircleShape)
                    .clip(CircleShape)
                    .clickable { onVideoCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Videocam, contentDescription = "Video call", tint = CallsPrimary, modifier = Modifier.size(20.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.1f), CircleShape)
                    .clip(CircleShape)
                    .clickable { onAudioCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Audio call", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
            }
        }
    }
}
