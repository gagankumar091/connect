package com.mitron.connect.ui.screens.aiassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitron.connect.data.model.ChatMessage
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

class AiViewModel(
    private val repository: com.mitron.connect.data.VercelRepository = com.mitron.connect.data.VercelRepository()
) : ViewModel() {
    // IMPORTANT: Replace with your actual Server IP
    private val ollamaUrl = "http://51.79.143.65:11434/api/chat"
    private val modelName = "qwen2:0.5b"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init",
                text = "Hello! I am your AI networking assistant running on Ollama. Ask me to help you write ice-breakers, summarize contact info, or prepare for meetings!",
                fromUser = false,
                isAiLabeled = true
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(prompt: String) {
        if (prompt.isBlank()) return

        val userMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), text = prompt, fromUser = true)
        _messages.value = _messages.value + userMsg

        if (ollamaUrl.contains("YOUR_SERVER_IP")) {
            _messages.value = _messages.value + ChatMessage(id = "error", text = "Please set your server IP in AiViewModel.kt.", fromUser = false, isAiLabeled = true)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val responseText = sendOllamaRequest(prompt)
                val aiMsg = ChatMessage(id = java.util.UUID.randomUUID().toString(), text = responseText, fromUser = false, isAiLabeled = true)
                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(id = "error", text = "Error: ${e.message}", fromUser = false, isAiLabeled = true)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun sendOllamaRequest(prompt: String): String = withContext(Dispatchers.IO) {
        val currentUserId = repository.getCurrentUserId()
        val currentUser = currentUserId?.let { repository.getContactById(it) }
        val allContacts = repository.getContacts()
        
        val contextStr = if (currentUser != null) {
            "User's Name: ${currentUser.name}. Profession: ${currentUser.title} at ${currentUser.company}. " +
            "Connections: ${allContacts.joinToString(", ") { it.name + " (" + it.title + ")" }}"
        } else {
            "Unknown user."
        }

        val systemPrompt = """
            You are the AI networking and relationship coach for the 'Mitron' professional app.
            Your ONLY purpose is to motivate the user to network, suggest actionable networking strategies, make predictions about professional relationships, and help them prepare for meetings.
            Here is the current user's LIVE data:
            $contextStr
            Use this data to give highly personalized advice, suggest who to connect with from their network, and provide tailored alerts.
            Provide concise, direct, and actionable advice. Do not output generic or irrelevant AI responses.
        """.trimIndent()

        val messagesArray = mutableListOf(
            buildJsonObject {
                put("role", "system")
                put("content", systemPrompt)
            }
        )

        messagesArray.addAll(_messages.value.map { msg ->
            buildJsonObject {
                put("role", if (msg.fromUser) "user" else "assistant")
                put("content", msg.text)
            }
        })
        
        messagesArray.add(
            buildJsonObject {
                put("role", "user")
                put("content", prompt)
            }
        )

        val jsonBody = buildJsonObject {
            put("model", modelName)
            put("stream", false)
            putJsonArray("messages") {
                messagesArray.forEach { add(it) }
            }
        }.toString()

        val request = Request.Builder()
            .url(ollamaUrl)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        val jsonElement = Json.parseToJsonElement(responseBody)
        val messageContent = jsonElement.jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content
        
        return@withContext messageContent ?: throw Exception("Failed to parse response")
    }
}
