package com.mitron.connect.ui.screens.chats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.services.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val contact by viewModel.contact.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var isUploading by remember { mutableStateOf(false) }
    
    var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showMessageActions by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploading = true
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            viewModel.uploadAttachment(chatId, tempFile, inputText)
            inputText = ""
            isUploading = false
        }
    }

    LaunchedEffect(chatId) {
        NotificationHelper.cancelChatNotifications(context, chatId)
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://api.dicebear.com/7.x/initials/svg?seed=${contact?.name ?: ""}",
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(contact?.name ?: "Chat", style = MaterialTheme.typography.titleMedium)
                            Text("Online", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { /* Call logic */ }) { Icon(Icons.Filled.Call, contentDescription = "Call") }
                    IconButton(onClick = { /* Video Call logic */ }) { Icon(Icons.Filled.Videocam, contentDescription = "Video Call") }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding().imePadding()) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        IsTypingWrapper(viewModel)
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { filePickerLauncher.launch("image/*") }) {
                                if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                else Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                            }
                            TextField(
                                value = inputText,
                                onValueChange = { 
                                    inputText = it
                                // viewModel.sendTypingEvent(chatId, true)
                            },
                            placeholder = { Text("Message...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4
                        )
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(chatId, inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = "Send", tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val groupedMessages = messages.groupBy { msg ->
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    val date = format.parse(msg.createdAt) ?: Date()
                    val today = Date()
                    if (date.date == today.date) "TODAY" else "OLDER"
                } catch(e:Exception) { "TODAY" }
            }

            groupedMessages.forEach { (dateHeader, msgs) ->
                stickyHeader {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(dateHeader, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                items(msgs, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message, 
                        contactName = contact?.name,
                        onLongClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedMessage = message
                            showMessageActions = true
                        }
                    )
                }
            }
        }
        
        if (showMessageActions && selectedMessage != null) {
            val clipboardManager: ClipboardManager = LocalClipboardManager.current
            ModalBottomSheet(onDismissRequest = { showMessageActions = false }) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Message Options", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    
                    ListItem(
                        headlineContent = { Text("Copy Text") },
                        leadingContent = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            clipboardManager.setText(AnnotatedString(selectedMessage!!.text))
                            showMessageActions = false
                        }
                    )
                    
                    if (selectedMessage!!.fromUser) {
                        ListItem(
                            headlineContent = { Text("Edit") },
                            leadingContent = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            modifier = Modifier.clickable { showMessageActions = false }
                        )
                        ListItem(
                            headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable { 
                                // viewModel.deleteMessage(selectedMessage!!.id)
                                showMessageActions = false 
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(message: ChatMessage, contactName: String?, onLongClick: () -> Unit = {}) {
    val isMine = message.fromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            AsyncImage(
                model = "https://api.dicebear.com/7.x/initials/svg?seed=$contactName",
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = message.text, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.align(Alignment.End)) {
                    val timeStr = try {
                        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                        val date = format.parse(message.createdAt) ?: Date()
                        SimpleDateFormat("h:mm a", Locale.US).format(date)
                    } catch(e:Exception) { "10:30 AM" }
                    
                    Text(text = timeStr, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead == 1) Icons.Filled.DoneAll else Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha1 by transition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(300, delayMillis = 0), RepeatMode.Reverse), label = "a1")
    val alpha2 by transition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(300, delayMillis = 150), RepeatMode.Reverse), label = "a2")
    val alpha3 by transition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(300, delayMillis = 300), RepeatMode.Reverse), label = "a3")

    Row(modifier = Modifier.padding(start = 56.dp, top = 4.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha1)))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha2)))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha3)))
    }
}

@Composable
fun IsTypingWrapper(viewModel: ChatViewModel) {
    val isTyping by viewModel.isTyping.collectAsState(initial = false)
    AnimatedVisibility(visible = isTyping) {
        TypingIndicator()
    }
}
