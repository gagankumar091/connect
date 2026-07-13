package com.mitron.connect.ui.screens.chats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.util.Date
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.blur

val SurfaceColor = Color(0xFFFAF8FF)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF2F3FF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceVariantColor = Color(0xFFDAE2FD)
val PrimaryColor = Color(0xFF0900DB)
val PrimaryContainer = Color(0xFF2D31FA)
val SecondaryContainer = Color(0xFF00CCF9)
val OutlineVariant = Color(0xFFC6C4DA)
val OutlineColor = Color(0xFF767589)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val SecondaryColor = Color(0xFF00677F)
val OnPrimaryColor = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFFC8CAFF)
val OnSecondaryContainer = Color(0xFF005266)
val BackgroundColor = Color(0xFFFAF8FF)
val ErrorColor = Color(0xFFBA1A1A)

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
            inputText = ""
            isUploading = false
        }
    }

    LaunchedEffect(chatId) {
        NotificationHelper.cancelChatNotifications(context, chatId)
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    Scaffold(
        containerColor = SurfaceContainerLowest,
        topBar = {
            Surface(
                color = SurfaceColor.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth().border(0.dp, Color.Transparent).background(SurfaceColor.copy(alpha = 0.8f)).shadow(0.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).offset(x = (-8).dp)) { 
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = OnSurfaceColor) 
                            }
                            Box {
                                AsyncImage(
                                    model = contact?.avatarUrl ?: "https://api.dicebear.com/7.x/initials/svg?seed=${contact?.name ?: ""}",
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant.copy(alpha = 0.2f), CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(12.dp)
                                        .background(SecondaryContainer, CircleShape)
                                        .border(2.dp, SurfaceColor, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(contact?.name ?: "Chat", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor, lineHeight = 20.sp)
                                Text("Online", fontSize = 12.sp, color = OnSurfaceVariantColor)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { /* Call */ }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.Call, contentDescription = "Call", tint = OnSurfaceColor) }
                            IconButton(onClick = { /* More */ }, modifier = Modifier.size(40.dp)) { Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = OnSurfaceColor) }
                        }
                    }
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor.copy(alpha = 0.9f))
            ) {
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Outlined.AttachFile, contentDescription = "Attach", tint = OnSurfaceVariantColor)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .background(SurfaceContainerHigh, RoundedCornerShape(24.dp))
                            .border(1.dp, if(inputText.isNotEmpty()) PrimaryContainer.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth().padding(end = 32.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = OnSurfaceColor),
                            cursorBrush = SolidColor(PrimaryColor),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text("Message...", color = OnSurfaceVariantColor.copy(alpha = 0.7f), fontSize = 16.sp)
                                }
                                innerTextField()
                            }
                        )
                        Icon(
                            Icons.Outlined.Mood,
                            contentDescription = "Emoji",
                            tint = OnSurfaceVariantColor,
                            modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).offset(y = (-2).dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(2.dp, CircleShape, spotColor = SecondaryContainer.copy(alpha = 0.2f))
                            .background(SecondaryContainer, CircleShape)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(chatId, inputText)
                                    inputText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (inputText.isNotBlank()) Icons.Filled.Send else Icons.Filled.Mic,
                            contentDescription = "Send",
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(innerPadding).fillMaxSize().background(SurfaceContainerLowest),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = dateHeader,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantColor,
                            letterSpacing = 1.sp,
                            modifier = Modifier.background(SurfaceContainerHigh, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                items(msgs, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message, 
                        onLongClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedMessage = message
                            showMessageActions = true
                        }
                    )
                }
            }

            item {
                AiSummaryCard()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp)) // padding block
            }
        }
        
        if (showMessageActions && selectedMessage != null) {
            val clipboardManager: ClipboardManager = LocalClipboardManager.current
            ModalBottomSheet(onDismissRequest = { showMessageActions = false }) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Message Options", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                    
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
fun ChatMessageBubble(message: ChatMessage, onLongClick: () -> Unit = {}) {
    val isMine = message.fromUser
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .combinedClickable(onClick = {}, onLongClick = onLongClick)
                    .shadow(1.dp, RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 2.dp,
                        bottomEnd = if (isMine) 2.dp else 16.dp
                    ), spotColor = Color.Black.copy(alpha = 0.05f))
                    .background(
                        if (isMine) SurfaceVariantColor else SurfaceContainerLow,
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 2.dp,
                            bottomEnd = if (isMine) 2.dp else 16.dp
                        )
                    )
                    .border(
                        1.dp,
                        if (isMine) Color.Transparent else OutlineVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 2.dp,
                            bottomEnd = if (isMine) 2.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 16.sp,
                    color = OnSurfaceColor,
                    lineHeight = 24.sp
                )
            }
            
            val timeStr = try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                val date = format.parse(message.createdAt) ?: Date()
                SimpleDateFormat("h:mm a", Locale.US).format(date)
            } catch(e:Exception) { "10:30 AM" }
            
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.padding(top = 4.dp, start = if(!isMine) 4.dp else 0.dp, end = if(isMine) 4.dp else 0.dp)
            ) {
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = OnSurfaceVariantColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (message.isRead == 1) Icons.Filled.DoneAll else Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (message.isRead == 1 && !isMine) SecondaryColor else OnSurfaceVariantColor
                )
            }
        }
    }
}

@Composable
fun AiSummaryCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = PrimaryContainer.copy(alpha = 0.03f))
            .clip(RoundedCornerShape(20.dp))
            .background(PrimaryContainer.copy(alpha = 0.05f))
            .border(1.dp, PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        // Decorative gradient blur
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .size(128.dp)
                .background(PrimaryContainer.copy(alpha = 0.1f), CircleShape)
                .blur(24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Summary", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor)
                }
                Box(modifier = Modifier.background(PrimaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("New", fontSize = 10.sp, color = OnPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 4.dp)) {
                AiSummaryItem("Exploring SAP integration")
                AiSummaryItem("Estimated budget: ~$100k")
                AiSummaryItem("Wants demo in August")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceColor, contentColor = OnSurfaceColor),
                    border = BorderStroke(1.dp, OutlineVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.TaskAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Task", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor, contentColor = OnPrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(40.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save to CRM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun AiSummaryItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.padding(top = 8.dp).size(6.dp).background(PrimaryColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = OnSurfaceColor,
            lineHeight = 22.sp
        )
    }
}
