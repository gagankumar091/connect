package com.mitron.connect.ui.screens.aiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.ChatMessage
import kotlinx.coroutines.delay

val BackgroundColor = Color(0xFFFAF8FF)
val SurfaceColor = Color(0xFFFAF8FF)
val SurfaceContainerColor = Color(0xFFEAEDFF)
val SurfaceContainerHigh = Color(0xFFE2E7FF)
val SurfaceVariantColor = Color(0xFFDAE2FD)
val PrimaryColor = Color(0xFF0900DB)
val PrimaryContainer = Color(0xFF2D31FA)
val OnSurfaceColor = Color(0xFF131B2E)
val OnSurfaceVariantColor = Color(0xFF454557)
val OutlineColor = Color(0xFF767589)
val OutlineVariant = Color(0xFFC6C4DA)

@Composable
fun AiAssistantScreen(
    modifier: Modifier = Modifier,
    viewModel: AiViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val quickActions = listOf(
        "Summarize all conversations with XYZ company",
        "Draft follow up message for Rahul",
        "Remind me after Ganesh Chaturthi"
    )

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
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Assistant",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp).background(Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = OnSurfaceVariantColor)
                }
            }
        },
        bottomBar = {
            // Note: The global BottomNavigation covers the bottom. 
            // We only need the Input Area floating above it.
            // Using a Box to render the input area at the bottom of the Scaffold's content.
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.4f))) // mock border

            // Chat Container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(messages) { message -> 
                    ChatBubble(message) 
                }
                
                if (isLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "AI is thinking...", 
                                fontSize = 14.sp, 
                                color = OnSurfaceVariantColor
                            )
                        }
                    }
                }

                // Push suggestions to the bottom visually if needed, but in a list they follow the chat.
                item {
                    var clickedAction by remember { mutableStateOf<String?>(null) }
                    Column(
                        modifier = Modifier.padding(top = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickActions.forEach { action ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isLoading) { 
                                        clickedAction = action
                                        viewModel.sendMessage(action) 
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isLoading && clickedAction == action) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(end = 8.dp).size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = PrimaryColor
                                        )
                                    }
                                    Text(
                                        text = action,
                                        color = PrimaryColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Area pinned at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor.copy(alpha = 0.9f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding() // just in case
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(SurfaceContainerHigh, RoundedCornerShape(26.dp))
                        .border(1.dp, if(inputText.isNotEmpty()) PrimaryColor else OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 40.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = OnSurfaceColor),
                        cursorBrush = SolidColor(PrimaryColor),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text("Ask anything...", color = OnSurfaceVariantColor, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                    
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Send, 
                            contentDescription = "Send", 
                            tint = if (inputText.isNotBlank()) PrimaryColor else OutlineColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.fromUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        if (isUser) {
            // User Message
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        PrimaryContainer.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 2.dp, // rounded-tr-sm
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = OnSurfaceColor,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        } else {
            // AI Response Card
            var displayedText by remember { mutableStateOf("") }
            LaunchedEffect(message.text) {
                if (message.text.length > displayedText.length) {
                    for (i in displayedText.length until message.text.length) {
                        displayedText += message.text[i]
                        if (i % 3 == 0) delay(10) // typewriter effect speed
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.Top
            ) {
                // AI Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // AI Message Bubble
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            SurfaceContainerColor,
                            shape = RoundedCornerShape(
                                topStart = 2.dp, // rounded-tl-sm
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .border(
                            1.dp, 
                            Color.White.copy(alpha = 0.4f), 
                            RoundedCornerShape(
                                topStart = 2.dp, 
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = displayedText,
                        color = OnSurfaceColor, 
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                    
                    // Specific logic for mock suggestion to match UI design exactly
                    if (message.text.contains("5 people") || message.text.contains("Here are")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceColor)
                                .border(1.dp, PrimaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { /* Show people logic */ }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Show People", color = PrimaryColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}