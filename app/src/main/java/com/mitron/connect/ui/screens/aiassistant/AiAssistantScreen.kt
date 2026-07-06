package com.mitron.connect.ui.screens.aiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mitron.connect.data.SampleData
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AiAssistantScreen(
    modifier: Modifier = Modifier,
    viewModel: AiViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(Spacing.md)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            contentPadding = PaddingValues(bottom = Spacing.sm)
        ) {
            items(messages) { message -> ChatBubble(message) }
            if (isLoading) {
                item {
                    Text("AI is typing...", style = MaterialTheme.typography.bodySmall, color = colors.textMuted, modifier = Modifier.padding(Spacing.sm))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface2, RoundedCornerShape(24.dp))
                .padding(horizontal = Spacing.sm, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask the assistant...", color = colors.textMuted) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )
            IconButton(
                onClick = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = colors.pro)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val colors = ConnectTheme.colors
    val alignment = if (message.fromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (message.fromUser) colors.fillPrimary else colors.proBg
    val fg = if (message.fromUser) colors.onPrimary else colors.pro

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = bg,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.fromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.fromUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            if (message.isAiLabeled) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = fg, modifier = Modifier.padding(end = 4.dp).size(14.dp))
                    Text("AI Assistant", style = MaterialTheme.typography.labelSmall, color = fg)
                }
            }
            Text(message.text, style = MaterialTheme.typography.bodyMedium, color = fg)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun AiAssistantScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        AiAssistantScreen()
    }
}