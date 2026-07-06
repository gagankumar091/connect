import os

base_dir = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens"

timeline_vm = """package com.mitron.connect.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.FirebaseRepository
import com.mitron.connect.data.model.TimelineEvent
import com.mitron.connect.data.model.TimelineIcon
import com.mitron.connect.data.model.MeetingSummary
import com.mitron.connect.data.model.AccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.UUID
import kotlinx.coroutines.tasks.await

class TimelineViewModel(private val repository: FirebaseRepository = FirebaseRepository()) : ViewModel() {
    private val _events = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val events: StateFlow<List<TimelineEvent>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val client = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()
    private val ollamaUrl = "http://51.79.143.65:11434/api/chat"

    fun loadTimeline(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _events.value = repository.getTimelineEvents(contactId).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logMeeting(contactId: String, rawNotes: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventId = UUID.randomUUID().toString()
                
                val prompt = "Extract and summarize these meeting notes. Format the response EXACTLY like this: SUMMARY: <brief summary> | PAIN: <one key pain point> | BUDGET: <budget if mentioned, else 'Unknown'> | ACTION: <next step>"
                val aiResponse = sendOllamaRequest("$prompt\\nNotes: $rawNotes")
                
                var sumText = "Meeting completed."
                var pain = "General"
                var budget = "Unknown"
                var action = "Follow up"

                if (aiResponse.contains("SUMMARY:")) {
                    val parts = aiResponse.split("|")
                    parts.forEach { part ->
                        if (part.contains("SUMMARY:")) sumText = part.substringAfter("SUMMARY:").trim()
                        if (part.contains("PAIN:")) pain = part.substringAfter("PAIN:").trim()
                        if (part.contains("BUDGET:")) budget = part.substringAfter("BUDGET:").trim()
                        if (part.contains("ACTION:")) action = part.substringAfter("ACTION:").trim()
                    }
                } else {
                    sumText = aiResponse.trim()
                }

                val summary = MeetingSummary(
                    id = eventId,
                    contactId = contactId,
                    summary = sumText,
                    painPoint = pain,
                    budget = budget,
                    actionItems = action
                )
                repository.db.collection("meeting_summaries").document(eventId).set(summary).await()

                val event = TimelineEvent(
                    id = eventId,
                    contactId = contactId,
                    label = "Coffee Meeting",
                    icon = TimelineIcon.COFFEE,
                    color = AccentColor.ACCENT,
                    isMeeting = true,
                    createdAt = java.util.Date()
                )
                repository.db.collection("timeline_events").document(eventId).set(event).await()
                
                loadTimeline(contactId)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun sendOllamaRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val jsonBody = buildJsonObject {
            put("model", "qwen2:0.5b")
            put("stream", false)
            putJsonArray("messages") {
                add(buildJsonObject { put("role", "system"); put("content", "You are an AI assistant that only responds in exactly the requested format.") })
                add(buildJsonObject { put("role", "user"); put("content", prompt) })
            }
        }.toString()
        val request = Request.Builder().url(ollamaUrl).post(jsonBody.toRequestBody("application/json".toMediaType())).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        val jsonElement = Json.parseToJsonElement(response.body?.string() ?: "")
        return@withContext jsonElement.jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content ?: ""
    }
}
"""

timeline_ui = """package com.mitron.connect.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mitron.connect.data.model.TimelineEvent
import com.mitron.connect.data.model.TimelineIcon
import com.mitron.connect.ui.components.ConnectPrimaryButton
import com.mitron.connect.ui.components.ConnectSecondaryButton
import com.mitron.connect.ui.components.ConnectTopBar
import com.mitron.connect.ui.theme.ConnectTheme
import com.mitron.connect.ui.theme.Spacing
import com.mitron.connect.ui.theme.tints

private fun TimelineIcon.imageVector(): ImageVector = when (this) {
    TimelineIcon.LOCATION -> Icons.Filled.LocationOn
    TimelineIcon.QR -> Icons.Filled.QrCode
    TimelineIcon.FILE -> Icons.Filled.Description
    TimelineIcon.COFFEE -> Icons.Filled.Coffee
    TimelineIcon.SEND -> Icons.Filled.Send
    TimelineIcon.CHECK -> Icons.Filled.Check
}

@Composable
fun TimelineScreen(
    contactId: String,
    onBack: () -> Unit,
    onOpenMeetingSummary: (String) -> Unit,
    viewModel: TimelineViewModel = viewModel()
) {
    val colors = ConnectTheme.colors
    
    androidx.compose.runtime.LaunchedEffect(contactId) {
        viewModel.loadTimeline(contactId)
    }
    
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Log a Meeting", color = colors.textPrimary) },
            text = {
                Column {
                    Text("Enter rough notes. The AI will summarize them.", color = colors.textMuted, modifier = Modifier.padding(bottom = 8.dp))
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth().size(120.dp),
                        placeholder = { Text("e.g. Met for coffee. Pain point: old software. Budget: $10k. Action: send demo.") }
                    )
                }
            },
            confirmButton = {
                ConnectPrimaryButton(text = "Log & Analyze") {
                    showDialog = false
                    viewModel.logMeeting(contactId, notes) {
                        notes = ""
                    }
                }
            },
            dismissButton = {
                ConnectSecondaryButton(text = "Cancel") { showDialog = false }
            }
        )
    }

    Scaffold(
        topBar = { ConnectTopBar(title = "Timeline", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colors.accent
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Meeting", tint = colors.surface1)
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = colors.accent)
            }
        } else if (events.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No timeline events yet", color = colors.textMuted, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(Spacing.md)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(events) { event ->
                    TimelineRow(event, onClick = { if (event.isMeeting) onOpenMeetingSummary(contactId) })
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent, onClick: () -> Unit) {
    val colors = ConnectTheme.colors
    val (bg, fg) = event.color.tints(colors)
    Row(
        modifier = Modifier.clickable(enabled = event.isMeeting, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(26.dp).background(bg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(event.icon.imageVector(), contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
        }
        Column(modifier = Modifier.padding(start = Spacing.xs)) {
            Text(event.label, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (event.isMeeting) {
                Text("Tap for AI meeting summary", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun TimelineScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        TimelineScreen(contactId = "preview", onBack = {}, onOpenMeetingSummary = {})
    }
}
"""

with open(os.path.join(base_dir, "timeline", "TimelineViewModel.kt"), "w") as f:
    f.write(timeline_vm)

with open(os.path.join(base_dir, "timeline", "TimelineScreen.kt"), "w") as f:
    f.write(timeline_ui)

print("Created Timeline logic with AI meeting logger!")
