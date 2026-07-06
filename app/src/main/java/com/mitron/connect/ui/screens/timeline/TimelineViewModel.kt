package com.mitron.connect.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.VercelRepository
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

class TimelineViewModel(private val repository: VercelRepository = VercelRepository()) : ViewModel() {
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
                val aiResponse = sendOllamaRequest("$prompt\nNotes: $rawNotes")
                
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
