package com.mitron.connect.ui.screens.chats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.services.NotificationHelper
import com.mitron.connect.ui.components.Avatar
import com.mitron.connect.ui.theme.AvatarSize
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints
import com.mitron.connect.data.model.AccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    val messages by viewModel.messages.collectAsState()
    val contact  by viewModel.contact.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(chatId) {
        NotificationHelper.cancelChatNotifications(context, chatId)
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val aiSummary by viewModel.aiSummary.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 0.dp,
                color = Color.White,
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF374151), // gray-700
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            val (avatarBg, avatarFg) = AccentColor.ACCENT.tints(colors)
                            Avatar(
                                initials = contact?.name?.take(2)?.uppercase() ?: "??",
                                size = AvatarSize.small,
                                background = Color(0xFFDBEAFE), // blue-100
                                foreground = Color(0xFF1E40AF), // blue-800
                            )
                            // Online dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(12.dp)
                                    .background(Color(0xFF22C55E), CircleShape) // green-500
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact?.name ?: "Chat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827), // gray-900
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Online",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF22C55E) // green-500
                            )
                        }

                        val scope = rememberCoroutineScope()
                        IconButton(onClick = {
                            contact?.id?.let { receiverId ->
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
                                                receiverId = receiverId,
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
                            }
                        }) {
                            Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color(0xFF4B5563), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = {
                            contact?.id?.let { receiverId ->
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
                                                receiverId = receiverId,
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
                            }
                        }) {
                            Icon(Icons.Filled.Videocam, contentDescription = "Video", tint = Color(0xFF4B5563), modifier = Modifier.size(22.dp))
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6))) // border-b gray-100
                }
            }
        },
        containerColor = Color(0xFFF3F4F6) // bg-gray-100
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // ── Messages list ──────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Date header (simplified)
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9FAFB) // gray-50
                        ) {
                            Text(
                                "TODAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9CA3AF), // gray-400
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = if (message.fromUser)
                            slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn()
                        else
                            slideInHorizontally(initialOffsetX = { -it / 2 }) + fadeIn(),
                    ) {
                        ChatMessageBubble(message = message, colors = colors)
                    }
                }
            }

            // ── AI Summary & Input Area ──────────────────────────────────
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Ensure input is not hidden by keyboard by using imePadding
                        .imePadding()
                        // Keep navigation bar padding so it stays above the gesture bar
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    if (aiSummary != null && aiSummary!!.isNotEmpty()) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Summary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = Color(0xFFDBEAFE), shape = CircleShape) {
                                    Text("New", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                            Icon(Icons.Filled.Star, contentDescription = "AI", tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                        }
                        
                        // Bullet Points
                        Column(modifier = Modifier.padding(bottom = 20.dp)) {
                            aiSummary!!.forEach { point ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 6.dp)) {
                                    Box(modifier = Modifier.padding(top = 6.dp).size(4.dp).background(Color(0xFF9CA3AF), CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(point, fontSize = 12.sp, color = Color(0xFF4B5563))
                                }
                            }
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Input field
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF3F4F6), // gray-100
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                TextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text("Message...", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color(0xFF111827),
                                        unfocusedTextColor = Color(0xFF111827),
                                        cursorColor = Color(0xFF2563EB)
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (inputText.isNotBlank()) {
                                            viewModel.sendMessage(chatId, inputText)
                                            inputText = ""
                                        }
                                    }),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach", tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Mic / Send button
                        val canSend = inputText.isNotBlank()
                        AnimatedContent(targetState = canSend, label = "send_btn") { sending ->
                            Surface(
                                shape = CircleShape,
                                color = if (sending) Color(0xFF2563EB) else Color(0xFF16A34A), // blue-600 vs green-600
                                modifier = Modifier.size(44.dp).clickable {
                                    if (sending) {
                                        viewModel.sendMessage(chatId, inputText)
                                        inputText = ""
                                    }
                                },
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (sending) Icons.Rounded.Send else Icons.Filled.Mic,
                                        contentDescription = if (sending) "Send" else "Mic",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage, colors: com.mitron.connect.ui.theme.ConnectColorScheme) {
    val isMine = message.fromUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMine) 56.dp else 0.dp,
                end   = if (isMine) 0.dp else 56.dp
            ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd   = 16.dp,
                    bottomStart = if (isMine) 16.dp else 0.dp,
                    bottomEnd   = if (isMine) 0.dp else 16.dp,
                ),
                color = if (isMine) Color(0xFFEFF6FF) else Color(0xFFF3F4F6), // blue-50 vs gray-100
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(
                        start  = 12.dp,
                        end    = 12.dp,
                        top    = 12.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(
                        text  = message.text,
                        color = Color(0xFF1F2937), // gray-800
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        // Extract time from ISO string, defaulting to "10:30 AM" fallback if parsing fails
                        val timeStr = try {
                            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                            val date = format.parse(message.createdAt) ?: java.util.Date()
                            java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(date)
                        } catch(e:Exception) { "10:30 AM" }
                        
                        Text(
                            text  = timeStr,
                            color = Color(0xFF9CA3AF), // gray-400
                            fontSize = 9.sp,
                        )
                        if (isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isRead == 1)
                                    Icons.Filled.DoneAll else Icons.Filled.Done,
                                contentDescription = if (message.isRead == 1) "Read" else "Sent",
                                tint = if (message.isRead == 1)
                                    Color(0xFF3B82F6) else Color(0xFF9CA3AF), // blue-500
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
