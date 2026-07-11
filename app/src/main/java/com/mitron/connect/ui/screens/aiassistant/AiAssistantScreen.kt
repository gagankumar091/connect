package com.mitron.connect.ui.screens.aiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.ChatMessage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ModelDropdown() {
        val models = listOf("llama3.2:1b", "phi3", "mistral")
        var expanded by remember { mutableStateOf(false) }
        // Read selected model from ViewModel in real implementation
        var selectedModel by remember { mutableStateOf(models[0]) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedModel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().width(150.dp).height(48.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                models.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model, fontSize = 12.sp) },
                        onClick = {
                            selectedModel = model
                            expanded = false
                            // Save to DataStore logic goes here
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // bg-gray-50
    ) {
        // Status bar padding (simulated for compose if needed, but HomeScreen might handle it)
        Spacer(modifier = Modifier.height(androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE0E7FF), // indigo-100
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "AI",
                        tint = Color(0xFF4F46E5), // indigo-600
                        modifier = Modifier.padding(6.dp).size(20.dp)
                    )
                }
                Text(
                    text = "AI Assistant",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827) // gray-900
                )
            }
            ModelDropdown()
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF3F4F6))) // border-b

        // Chat Container
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(messages) { message -> 
                ChatBubble(message) 
            }
            
            if (isLoading) {
                item {
                    Text(
                        "AI is typing...", 
                        fontSize = 12.sp, 
                        color = Color(0xFF9CA3AF), 
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // Quick Actions
            item {
                var clickedAction by remember { mutableStateOf<String?>(null) }
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quickActions.forEach { action ->
                        Surface(
                            color = Color(0xFFF3F4F6), // gray-100
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .clickable(enabled = !isLoading) { 
                                    clickedAction = action
                                    viewModel.sendMessage(action) 
                                }
                                .padding(end = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLoading && clickedAction == action) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(start = 12.dp).size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF4F46E5)
                                    )
                                }
                                Text(
                                    text = action,
                                    color = if (isLoading && clickedAction != action) Color.Gray else Color(0xFF4F46E5), // indigo-600
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Area
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything...", color = Color(0xFF6B7280), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(end = 4.dp), // make room for button
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF1F2937),
                        unfocusedTextColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true
                )
                
                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(44.dp)
                        .background(Color(0xFF4F46E5), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Send, 
                        contentDescription = "Send", 
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(-45f) // makes it look like a paper plane pointing up-right
                    )
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
            Surface(
                color = Color(0xFF4F46E5), // indigo-600
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 0.dp, // rounded-tr-none
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)), // border-gray-100
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(0.9f) // max-w-[90%]
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = displayedText,
                        color = Color(0xFF374151), // gray-700
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    
                    // Specific logic for mock suggestion to match UI design exactly
                    if (message.text.contains("5 people") || message.text.contains("Here are")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = Color(0xFFEEF2FF), // indigo-50
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E7FF)), // indigo-100
                            modifier = Modifier.fillMaxWidth().clickable { /* Show people logic */ }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                                Text("Show People", color = Color(0xFF4338CA), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun AiAssistantScreenPreview() {
    AiAssistantScreen()
}